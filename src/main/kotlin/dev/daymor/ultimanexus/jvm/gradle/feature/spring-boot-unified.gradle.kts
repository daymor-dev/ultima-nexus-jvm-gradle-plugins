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

import dev.daymor.ultimanexus.jvm.gradle.util.DependencyUtils.Fallbacks
import dev.daymor.ultimanexus.jvm.gradle.util.DependencyUtils.getLibraryOrNull

/**
 * Unified Spring Boot feature plugin with configurable application/library mode.
 *
 * Default: Application mode (bootJar and bootRun enabled).
 *
 * Configuration via build.gradle.kts:
 * ```kotlin
 * springBootConfig {
 *     isApplication.set(false)  // library mode
 * }
 * ```
 *
 * Or via gradle.properties:
 * ```properties
 * springBoot.isApplication=false
 * ```
 *
 * Usage:
 * plugins {
 *     id("dev.daymor.ultimanexus.jvm.gradle.feature.spring-boot-unified")
 * }
 */
plugins {
    java
    id("org.springframework.boot")
    id("dev.daymor.ultimanexus.jvm.gradle.base.dependency-rules")
}

/**
 * Extension to configure Spring Boot mode.
 */
interface SpringBootConfigExtension {
    /**
     * Whether this is a Spring Boot application (true) or library (false).
     *
     * Application mode (default):
     * - Enables bootJar and bootRun tasks
     * - Creates executable fat JAR
     * - Uses productionRuntimeClasspath
     *
     * Library mode:
     * - Disables bootJar and bootRun tasks
     * - Creates standard JAR
     * - Uses standard java-library configurations
     */
    val isApplication: Property<Boolean>
}

val springBootConfig = extensions.create<SpringBootConfigExtension>("springBootConfig")

// Read from gradle.properties with default true (application mode)
val isApplicationFromProps = providers.gradleProperty("springBoot.isApplication").orNull?.toBoolean()
springBootConfig.isApplication.convention(isApplicationFromProps ?: true)

val libs: VersionCatalog = versionCatalogs.named("libs")

// Configure after evaluation when extension values are finalized
afterEvaluate {
    val isApp = springBootConfig.isApplication.get()

    if (isApp) {
        // Application mode: configure productionRuntimeClasspath
        configurations {
            val internalConfig = configurations.findByName("internal")
            val mainRuntimeConfig = configurations.findByName("mainRuntimeClasspath")

            productionRuntimeClasspath {
                internalConfig?.let { extendsFrom(it) }
                mainRuntimeConfig?.let { shouldResolveConsistentlyWith(it) }
            }
            findByName("developmentOnly")?.apply {
                internalConfig?.let { extendsFrom(it) }
                mainRuntimeConfig?.let { shouldResolveConsistentlyWith(it) }
            }
        }
    } else {
        // Library mode: disable bootJar, enable jar
        tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
            enabled = false
        }
        tasks.named<Jar>("jar") {
            enabled = true
            archiveClassifier.set("")  // Remove 'plain' classifier
        }

        // Disable bootRun task if it exists
        tasks.findByName("bootRun")?.enabled = false
    }
}

// Add common dependencies for both modes
dependencies {
    compileOnly(getLibraryOrNull(libs, "jspecify") ?: Fallbacks.JSPECIFY)
    compileOnly(getLibraryOrNull(libs, "jsr305") ?: Fallbacks.JSR305)
    developmentOnly(getLibraryOrNull(libs, "spring-boot-devtools") ?: Fallbacks.SPRING_BOOT_DEVTOOLS)
}
