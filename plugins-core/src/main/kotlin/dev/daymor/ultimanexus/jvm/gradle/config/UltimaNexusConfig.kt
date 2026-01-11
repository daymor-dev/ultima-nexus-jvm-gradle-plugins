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

package dev.daymor.ultimanexus.jvm.gradle.config

import org.gradle.api.Project
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider

abstract class UltimaNexusConfig(private val project: Project) {

    abstract val groupId: Property<String>
    abstract val version: Property<String>
    abstract val description: Property<String>
    abstract val pluginWebsite: Property<String>
    abstract val pluginVcsUrl: Property<String>
    abstract val pluginTags: ListProperty<String>
    abstract val pluginIdPrefix: Property<String>
    abstract val checkArtifactName: Property<String>
    abstract val applicationMode: Property<Boolean>

    init {
        groupId.convention(getPropertyProvider(PropertyKeys.Identity.GROUP_ID))
        version.convention(getPropertyProvider(PropertyKeys.Identity.VERSION))
        description.convention(getPropertyProvider(PropertyKeys.Identity.DESCRIPTION))
        pluginWebsite.convention(getPropertyProvider(PropertyKeys.Plugin.WEBSITE))
        pluginVcsUrl.convention(getPropertyProvider(PropertyKeys.Plugin.VCS_URL))
        pluginTags.convention(
            getPropertyProvider(PropertyKeys.Plugin.TAGS).map { it.split(",").map { tag -> tag.trim() } }
        )
        pluginIdPrefix.convention(getPropertyProvider(PropertyKeys.Plugin.ID_PREFIX))
        checkArtifactName.convention(getPropertyProvider(PropertyKeys.CHECK_ARTIFACT_NAME))
        applicationMode.convention(resolveApplicationMode())
    }

    private fun resolveApplicationMode(): Boolean {
        val javaIsApp = project.providers.gradleProperty(PropertyKeys.JAVA_IS_APPLICATION).orNull?.toBoolean()
        val springBootIsApp = project.providers.gradleProperty(PropertyKeys.SpringBoot.IS_APPLICATION).orNull?.toBoolean()
        return javaIsApp ?: springBootIsApp ?: true
    }

    private fun getPropertyProvider(key: String): Provider<String> =
        project.providers.gradleProperty(key).orElse(
            project.provider { if (project.hasProperty(key)) project.property(key)?.toString() else null }
        )

    companion object {
        const val NAME = "ultimaNexus"

        fun get(project: Project): UltimaNexusConfig =
            project.extensions.findByType(UltimaNexusConfig::class.java)
                ?: project.extensions.create(NAME, UltimaNexusConfig::class.java, project)
    }
}
