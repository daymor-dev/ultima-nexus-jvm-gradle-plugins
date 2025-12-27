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

package dev.daymor.ultimanexus.jvm.gradle.feature

import dev.daymor.ultimanexus.jvm.gradle.util.ProjectUtils.includeDir

/**
 * Settings plugin for automatic project structure discovery.
 * Configures Maven Central repository, enables typesafe project accessors,
 * and automatically includes all subprojects found within the configured depth.
 *
 * Configuration via gradle.properties:
 *   projectStructureDepth=6  # Maximum directory depth to search (default: 1)
 *   rootProjectName=my-project  # Root project name (default: directory name)
 *
 * Usage in settings.gradle.kts:
 * plugins {
 *     id("dev.daymor.ultimanexus.jvm.gradle.feature.project-structure")
 * }
 */
dependencyResolutionManagement.repositories.mavenCentral()

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

val depth = settings.providers.gradleProperty("projectStructureDepth")
    .orElse("1")
    .get()
    .toInt()

includeDir(depth)

// Set rootProject.name from gradle.properties if not explicitly set
if (rootProject.name == rootDir.name) {
    val propertyName = settings.providers.gradleProperty("rootProjectName")
    if (propertyName.isPresent) {
        rootProject.name = propertyName.get()
    }
}
