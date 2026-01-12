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

/**
 * Settings plugin for Gradle composite build projects (like plugin development).
 * Automatically discovers composite builds, shares version catalog, and loads shared properties.
 *
 * Configuration via gradle.properties:
 *   rootProjectName=my-plugins           # Root project name (required)
 *   compositeBuildPrefix=plugins-        # Prefix for auto-discovery (optional)
 *   sharedGradlePath=../gradle/          # Path to shared gradle folder (default: ../gradle/ for subprojects)
 *   includedBuilds=build-logic,common    # Explicit composite builds for subprojects (optional)
 *
 * Features:
 *   - Auto-discovers composite builds by prefix in root project
 *   - Shares version catalog from gradle/libs.versions.toml
 *   - Loads shared properties from gradle/shared.properties
 *   - Applies org.gradle.* properties as system properties
 *
 * Usage in settings.gradle.kts:
 * ```kotlin
 * plugins {
 *     id("dev.daymor.ultimanexus.jvm.gradle.feature.composite-build") version "latest.release"
 * }
 * ```
 */

val isRootProject = file("gradle/libs.versions.toml").exists()

val sharedGradlePath = if (isRootProject) {
    "gradle/"
} else {
    providers.gradleProperty(PropertyKeys.Build.SHARED_GRADLE_PATH)
        .orElse(Defaults.SHARED_GRADLE_PATH)
        .get()
}

val sharedPropsFile = file("${sharedGradlePath}shared.properties")
val sharedProps = java.util.Properties()
if (sharedPropsFile.exists()) {
    sharedPropsFile.inputStream().use { sharedProps.load(it) }

    sharedProps.forEach { key, value ->
        if (key.toString().startsWith("org.gradle.")) {
            System.setProperty(key.toString(), value.toString())
        }
    }
}

gradle.beforeProject {
    sharedProps.forEach { key, value ->
        val keyStr = key.toString()
        if (!keyStr.startsWith("org.gradle.") && !project.hasProperty(keyStr)) {
            project.extensions.extraProperties[keyStr] = value.toString()
        }
    }
}

dependencyResolutionManagement {
    repositories {
        gradlePluginPortal()
    }
    if (!isRootProject) {
        versionCatalogs {
            create("libs") {
                from(files("${sharedGradlePath}libs.versions.toml"))
            }
        }
    }
}

if (!isRootProject) {
    providers.gradleProperty(PropertyKeys.Build.INCLUDED_BUILDS)
        .orNull
        ?.split(",")
        ?.map { it.trim() }
        ?.filter { it.isNotEmpty() }
        ?.forEach { includeBuild("../$it") }
}

if (isRootProject) {
    val prefix = providers.gradleProperty(PropertyKeys.Build.COMPOSITE_BUILD_PREFIX).orNull

    rootDir.listFiles()
        ?.filter { dir ->
            dir.isDirectory &&
                (prefix == null || dir.name.startsWith(prefix)) &&
                file("${dir.name}/settings.gradle.kts").exists()
        }
        ?.forEach { includeBuild(it.name) }
}

rootProject.name = providers.gradleProperty(PropertyKeys.Identity.ROOT_PROJECT_NAME).get()
