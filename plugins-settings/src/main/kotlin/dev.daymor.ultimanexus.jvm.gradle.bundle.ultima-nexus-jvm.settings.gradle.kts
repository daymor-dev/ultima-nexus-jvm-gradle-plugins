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


import dev.daymor.ultimanexus.jvm.gradle.config.Defaults
import dev.daymor.ultimanexus.jvm.gradle.config.PluginIds
import dev.daymor.ultimanexus.jvm.gradle.config.PropertyKeys

/**
 * Settings bundle plugin for Ultima Nexus JVM projects.
 * Applies project structure discovery, Git hooks configuration,
 * and conditionally includes Maven Central publishing aggregation.
 *
 * Included plugins (always):
 *   - dev.daymor.ultimanexus.jvm.gradle.feature.project-structure
 *   - dev.daymor.ultimanexus.jvm.gradle.feature.git-hooks
 *   - dev.daymor.ultimanexus.jvm.gradle.feature.build-cache
 *   - dev.daymor.ultimanexus.jvm.gradle.report.develocity
 *
 * Included plugins (conditional):
 *   - dev.daymor.ultimanexus.jvm.gradle.feature.publish-maven-central
 *     (automatically applied if any publish plugin is detected in build files)
 *
 * Usage in settings.gradle.kts:
 * ```kotlin
 * plugins {
 *     id("dev.daymor.ultimanexus.jvm.gradle.bundle.ultima-nexus-jvm") version "latest.release"
 * }
 * ```
 *
 * Configuration via gradle.properties:
 * ```properties
 * # Project structure settings
 * projectStructureDepth=1
 * projectStructureExclusions=gradle/plugins,*-gradle-plugins,build
 * rootProjectName=my-project
 *
 * # Maven Central credentials (required for publishing)
 * mavenCentralUsername=<your-token-username>
 * mavenCentralPassword=<your-token-password>
 * ```
 *
 * Features inherited:
 *   - Auto-discovers and includes all subprojects (project-structure)
 *   - Enables TYPESAFE_PROJECT_ACCESSORS (project-structure)
 *   - Installs Git pre-commit hooks running qualityCheck (git-hooks)
 *   - Auto-configures Maven Central publishing when publish plugins are detected
 */
plugins {
    id("dev.daymor.ultimanexus.jvm.gradle.feature.project-structure")
    id("dev.daymor.ultimanexus.jvm.gradle.feature.git-hooks")
    id("dev.daymor.ultimanexus.jvm.gradle.feature.build-cache")
    id("dev.daymor.ultimanexus.jvm.gradle.report.develocity")
}

private val PUBLISH_PATTERNS = listOf(
    "feature.publish-java",
    "feature.publish",
    "bundle.java-complete",
    "bundle.spring-boot-complete",
    "bundle.ultima-nexus"
)

// Extract depth at settings time to avoid capturing settings object in function closure
private val depth = settings.providers.gradleProperty(PropertyKeys.Build.PROJECT_STRUCTURE_DEPTH)
    .orElse(Defaults.PROJECT_STRUCTURE_DEPTH.toString())
    .get()
    .toInt()

fun detectPublishUsage(): Boolean {
    return rootDir.walk()
        .maxDepth(depth)
        .filter { it.name == "build.gradle.kts" }
        .any { file ->
            val content = file.readText()
            PUBLISH_PATTERNS.any { pattern -> content.contains(pattern) }
        }
}

if (detectPublishUsage()) {
    apply(plugin = PluginIds.Feature.PUBLISH_MAVEN_CENTRAL)
}
