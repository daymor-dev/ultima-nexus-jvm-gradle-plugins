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


import dev.daymor.ultimanexus.jvm.gradle.util.DependencyUtils.Fallbacks
import dev.daymor.ultimanexus.jvm.gradle.util.DependencyUtils.getLibraryOrNull
import dev.daymor.ultimanexus.jvm.gradle.util.DependencyUtils.getLibsCatalogOrNull

/**
 * Plugin: dev.daymor.ultimanexus.jvm.gradle.feature.spring-boot-application
 *
 * Feature plugin for Spring Boot application development.
 * Applies org.springframework.boot plugin with full application support.
 *
 * Features:
 * - bootJar and bootRun tasks enabled
 * - Creates executable fat JAR
 * - Configures productionRuntimeClasspath and developmentOnly
 * - Adds Spring Boot DevTools for development
 * - Spring Boot BOM for dependency management
 *
 * Usage:
 * ```kotlin
 * plugins {
 *     alias(libs.plugins.ultimanexus.feature.spring.boot.application)
 * }
 * ```
 */
plugins {
    java
    id("org.springframework.boot")
    id("dev.daymor.ultimanexus.jvm.gradle.base.dependency-rules")
}

val libs: VersionCatalog? = getLibsCatalogOrNull(project)

val springBootBom = libs?.let { getLibraryOrNull(it, "spring-boot-dependencies") }
    ?: Fallbacks.SPRING_BOOT_BOM

dependencies {
    add("implementation", platform(springBootBom))
}

val internalConfig = configurations.findByName("internal")
val mainRuntimeConfig = configurations.findByName("mainRuntimeClasspath")

configurations.named("productionRuntimeClasspath") {
    internalConfig?.let { extendsFrom(it) }
    mainRuntimeConfig?.let { shouldResolveConsistentlyWith(it) }
}
configurations.findByName("developmentOnly")?.apply {
    internalConfig?.let { extendsFrom(it) }
    mainRuntimeConfig?.let { shouldResolveConsistentlyWith(it) }
}

dependencies {
    add("developmentOnly", platform(springBootBom))
}

val devtools = libs?.let { getLibraryOrNull(it, "spring-boot-devtools") }
    ?: Fallbacks.SPRING_BOOT_DEVTOOLS
dependencies {
    add("developmentOnly", devtools)
}

val jspecify = libs?.let { getLibraryOrNull(it, "jspecify") } ?: Fallbacks.JSPECIFY
val jsr305 = libs?.let { getLibraryOrNull(it, "jsr305") } ?: Fallbacks.JSR305

dependencies {
    add("compileOnly", jspecify)
    add("compileOnly", jsr305)
}
