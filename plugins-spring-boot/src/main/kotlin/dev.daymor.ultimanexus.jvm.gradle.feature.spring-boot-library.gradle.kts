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
 * Plugin: dev.daymor.ultimanexus.jvm.gradle.feature.spring-boot-library
 *
 * Feature plugin for Spring Boot library/starter development.
 * Uses Spring Dependency Management without the full Spring Boot plugin.
 *
 * Features:
 * - Creates standard JAR (not fat JAR)
 * - Uses Spring Boot BOM for dependency management
 * - No bootJar or bootRun tasks
 * - Suitable for Spring Boot starters and shared libraries
 *
 * Usage:
 * ```kotlin
 * plugins {
 *     alias(libs.plugins.ultimanexus.feature.spring.boot.library)
 * }
 * ```
 */
plugins {
    `java-library`
    id("io.spring.dependency-management")
    id("dev.daymor.ultimanexus.jvm.gradle.base.dependency-rules")
}

val libs: VersionCatalog? = getLibsCatalogOrNull(project)

val springBootBom = libs?.let { getLibraryOrNull(it, "spring-boot-dependencies") }
    ?: Fallbacks.SPRING_BOOT_BOM

dependencies {
    add("implementation", platform(springBootBom))
}

val jspecify = libs?.let { getLibraryOrNull(it, "jspecify") } ?: Fallbacks.JSPECIFY
val jsr305 = libs?.let { getLibraryOrNull(it, "jsr305") } ?: Fallbacks.JSR305

dependencies {
    add("compileOnly", jspecify)
    add("compileOnly", jsr305)
}
