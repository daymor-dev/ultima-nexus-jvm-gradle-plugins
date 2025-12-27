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

import dev.daymor.ultimanexus.jvm.gradle.util.DependencyUtils.getLibrary

plugins {
    `java-platform`
    id("dev.daymor.ultimanexus.jvm.gradle.base.dependency-rules")
    id("dev.daymor.ultimanexus.jvm.gradle.base.lifecycle")
    id("dev.daymor.ultimanexus.jvm.gradle.feature.use-all-catalog-versions")
    id("dev.daymor.ultimanexus.jvm.gradle.check.format-gradle")
}

javaPlatform.allowDependencies()

/**
 * Configuration class for the version-platform plugin.
 * Allows configuring which BOMs to include in the platform.
 *
 * Example usage in build.gradle.kts:
 * ```kotlin
 * plugins {
 *     id("dev.daymor.ultimanexus.jvm.gradle.feature.version-platform")
 * }
 *
 * versionPlatform {
 *     includeSpringBootBom = true
 * }
 * ```
 *
 * Or via gradle.properties:
 * ```properties
 * versionPlatform.includeSpringBootBom=true
 * versionPlatform.springBootBomLibrary=spring-boot-dependencies
 * ```
 */
open class VersionPlatformConfig {
    /**
     * Whether to include the Spring Boot BOM as a dependency.
     * Default: true
     */
    var includeSpringBootBom: Boolean = true

    /**
     * The catalog library name for the Spring Boot dependencies BOM.
     * Default: "spring-boot-dependencies"
     */
    var springBootBomLibrary: String = "spring-boot-dependencies"
}

val versionPlatform = extensions.create<VersionPlatformConfig>("versionPlatform")

// Read from gradle.properties with defaults
val includeSpringBootBomFromProps = providers.gradleProperty("versionPlatform.includeSpringBootBom").orNull?.toBoolean()
val springBootBomLibraryFromProps = providers.gradleProperty("versionPlatform.springBootBomLibrary").orNull

// Apply gradle.properties values if extension wasn't explicitly configured
if (includeSpringBootBomFromProps != null) {
    versionPlatform.includeSpringBootBom = includeSpringBootBomFromProps
}
if (springBootBomLibraryFromProps != null) {
    versionPlatform.springBootBomLibrary = springBootBomLibraryFromProps
}

val libs: VersionCatalog = versionCatalogs.named("libs")

afterEvaluate {
    if (versionPlatform.includeSpringBootBom) {
        dependencies {
            api(platform(getLibrary(libs, versionPlatform.springBootBomLibrary)))
        }
    }
}
