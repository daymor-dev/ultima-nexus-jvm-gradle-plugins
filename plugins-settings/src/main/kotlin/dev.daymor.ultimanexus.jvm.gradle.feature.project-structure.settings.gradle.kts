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
import dev.daymor.ultimanexus.jvm.gradle.config.PropertyKeys
import dev.daymor.ultimanexus.jvm.gradle.util.ProjectUtils.includeDir

/**
 * Settings plugin for automatic project structure discovery.
 * Configures Maven Central repository, enables typesafe project accessors,
 * and automatically includes all subprojects found within the configured depth.
 *
 * Features:
 *   - Auto-discovers and includes subprojects based on directory structure
 *   - Enables TYPESAFE_PROJECT_ACCESSORS feature preview
 *   - Configures Maven Central for dependency resolution
 *
 * Usage in settings.gradle.kts:
 * ```kotlin
 * plugins {
 *     alias(libs.plugins.ultimanexus.jvm.feature.project.structure)
 * }
 * ```
 *
 * Configuration via gradle.properties:
 * ```properties
 * # Maximum directory depth to search for subprojects (default: 1)
 * projectStructureDepth=3
 *
 * # Comma-separated exclusion patterns (default: gradle/plugins,*-gradle-plugins)
 * projectStructureExclusions=gradle/plugins,*-gradle-plugins,build
 *
 * # Root project name (default: directory name)
 * rootProjectName=my-project
 * ```
 *
 * Example multi-module project:
 * ```
 * my-project/
 * ├── settings.gradle.kts    # Apply this plugin
 * ├── gradle.properties      # projectStructureDepth=3
 * ├── core/
 * │   └── build.gradle.kts   # Included as :core
 * ├── api/
 * │   └── build.gradle.kts   # Included as :api
 * └── modules/
 *     ├── web/
 *     │   └── build.gradle.kts  # Included as :modules:web
 *     └── data/
 *         └── build.gradle.kts  # Included as :modules:data
 * ```
 */
dependencyResolutionManagement.repositories.mavenCentral()

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

val depth = settings.providers.gradleProperty(PropertyKeys.Build.PROJECT_STRUCTURE_DEPTH)
    .orElse(Defaults.PROJECT_STRUCTURE_DEPTH.toString())
    .get()
    .toInt()

includeDir(depth)

if (rootProject.name == rootDir.name) {
    val propertyName = settings.providers.gradleProperty(PropertyKeys.Identity.ROOT_PROJECT_NAME)
    if (propertyName.isPresent) {
        rootProject.name = propertyName.get()
    }
}
