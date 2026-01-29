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


import dev.daymor.ultimanexus.jvm.gradle.config.PropertyKeys
import dev.daymor.ultimanexus.jvm.gradle.util.DependencyUtils.getLibraryOrNull
import dev.daymor.ultimanexus.jvm.gradle.util.DependencyUtils.getLibsCatalogOrNull
import dev.daymor.ultimanexus.jvm.gradle.util.PropertyUtils.findPropertyOrNull

/**
 * Version platform plugin that creates a BOM-style platform for dependency management.
 * Includes all catalog versions and optionally the Spring Boot BOM.
 *
 * Spring Boot BOM behavior:
 *   - Default: false (not included)
 *   - Auto-enabled when org.springframework.boot plugin is applied
 *   - Can be manually overridden via extension or gradle.properties
 *
 * Included plugins:
 *   - java-platform
 *   - dev.daymor.ultimanexus.jvm.gradle.base.dependency-rules
 *   - dev.daymor.ultimanexus.jvm.gradle.base.lifecycle
 *   - dev.daymor.ultimanexus.jvm.gradle.feature.use-all-catalog-versions
 *   - dev.daymor.ultimanexus.jvm.gradle.check.format-gradle
 *
 * Usage:
 *   plugins {
 *       id("dev.daymor.ultimanexus.jvm.gradle.feature.version-platform")
 *   }
 *
 *   versionPlatform {
 *       includeSpringBootBom = true  // optional, auto-enabled with Spring Boot
 *       springBootBomLibrary = "spring-boot-dependencies"
 *   }
 *
 * Properties (gradle.properties):
 *   versionPlatform.includeSpringBootBom=true
 *   versionPlatform.springBootBomLibrary=spring-boot-dependencies
 */
plugins {
    `java-platform`
    id("dev.daymor.ultimanexus.jvm.gradle.base.dependency-rules")
    id("dev.daymor.ultimanexus.jvm.gradle.base.lifecycle")
    id("dev.daymor.ultimanexus.jvm.gradle.feature.use-all-catalog-versions")
    id("dev.daymor.ultimanexus.jvm.gradle.check.format-gradle")
}

javaPlatform.allowDependencies()

open class VersionPlatformConfig {
    var includeSpringBootBom: Boolean = false
    var springBootBomLibrary: String = "spring-boot-dependencies"
}

val versionPlatform = extensions.create<VersionPlatformConfig>("versionPlatform")

// Auto-enable Spring Boot BOM when Spring Boot plugin is applied
pluginManager.withPlugin("org.springframework.boot") {
    versionPlatform.includeSpringBootBom = true
}

val includeSpringBootBomFromProps = project.findPropertyOrNull(PropertyKeys.VersionPlatform.INCLUDE_SPRING_BOOT_BOM)?.toBoolean()
val springBootBomLibraryFromProps = project.findPropertyOrNull(PropertyKeys.VersionPlatform.SPRING_BOOT_BOM_LIBRARY)

if (includeSpringBootBomFromProps != null) {
    versionPlatform.includeSpringBootBom = includeSpringBootBomFromProps
}
if (springBootBomLibraryFromProps != null) {
    versionPlatform.springBootBomLibrary = springBootBomLibraryFromProps
}

val libs: VersionCatalog? = getLibsCatalogOrNull(project)

if (versionPlatform.includeSpringBootBom) {
    libs?.let { catalog ->
        getLibraryOrNull(catalog, versionPlatform.springBootBomLibrary)?.let { bomLibrary ->
            dependencies {
                api(platform(bomLibrary))
            }
        }
    }
}
