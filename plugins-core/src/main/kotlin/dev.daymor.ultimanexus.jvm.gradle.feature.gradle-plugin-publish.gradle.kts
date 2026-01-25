/*
 * Copyright (C) 2025 Malcolm Rozé.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import dev.daymor.ultimanexus.jvm.gradle.config.UltimaNexusConfig
import dev.daymor.ultimanexus.jvm.gradle.util.PropertyUtils.findPropertyOrNull

/**
 * Convention plugin for publishing Gradle plugins to the Gradle Plugin Portal.
 *
 * This plugin automatically configures:
 * - Group ID and version from gradle.properties or extra properties
 * - Website and VCS URL from gradle.properties (pluginWebsite, pluginVcsUrl)
 * - Plugin metadata (displayName, description, tags) from gradle.properties
 * - Plugin ID prefix is derived from groupId (dashes removed, trailing dot added)
 *
 * Required gradle.properties or shared.properties:
 * ```properties
 * groupId = your.group.id
 * version = latest.release
 * pluginWebsite = https://your-website.com
 * pluginVcsUrl = https://github.com/your/repo
 * pluginTags = java,convention,quality
 *
 * # Per-plugin metadata (suffix is plugin ID after removing prefix)
 * plugin.my-plugin.displayName = My Plugin
 * plugin.my-plugin.description = Does something useful
 * ```
 */
plugins {
    id("com.gradle.plugin-publish")
}

val ultimaNexus = UltimaNexusConfig.get(project)

ultimaNexus.groupId.orNull?.let { group = it }
ultimaNexus.version.orNull?.let { version = it }

gradlePlugin {
    website = ultimaNexus.pluginWebsite.get()
    vcsUrl = ultimaNexus.pluginVcsUrl.get()

    plugins.configureEach {
        val pluginIdPrefix = ultimaNexus.pluginIdPrefix.get()
        val propertyKey = "plugin." + id.removePrefix(pluginIdPrefix)

        displayName = project.findPropertyOrNull("$propertyKey.displayName")
            ?: error("Missing property: $propertyKey.displayName")
        description = project.findPropertyOrNull("$propertyKey.description")
            ?: error("Missing property: $propertyKey.description")
        tags = ultimaNexus.pluginTags.get()
    }
}

publishing {
    publications.withType<MavenPublication>().configureEach {
        versionMapping {
            allVariants {
                fromResolutionResult()
            }
        }
    }
}
