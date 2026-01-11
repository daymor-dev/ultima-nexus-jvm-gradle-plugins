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


import dev.daymor.ultimanexus.jvm.gradle.config.UltimaNexusConfig
import dev.daymor.ultimanexus.jvm.gradle.util.DependencyUtils.Fallbacks
import dev.daymor.ultimanexus.jvm.gradle.util.DependencyUtils.getLibraryOrNull
import dev.daymor.ultimanexus.jvm.gradle.util.DependencyUtils.getLibsCatalogOrNull

/*
 * Feature plugin for unified Spring Boot development with configurable application/library mode.
 * Defaults to application mode with bootJar and bootRun enabled.
 *
 * Extension configuration (build.gradle.kts):
 *
 *     ultimaNexus {
 *         applicationMode.set(false)
 *     }
 *
 * Properties configuration (gradle.properties):
 *
 *     springBoot.isApplication=false
 *
 * Application mode (default):
 * - Enables bootJar and bootRun tasks
 * - Creates executable fat JAR
 * - Configures productionRuntimeClasspath
 *
 * Library mode:
 * - Disables bootJar and bootRun tasks
 * - Creates standard JAR without classifier
 * - Uses standard java-library configurations
 *
 * Usage:
 * ```kotlin
 * plugins {
 *     alias(libs.plugins.ultimanexus.jvm.feature.spring.boot.unified)
 * }
 * ```
 */

plugins {
    java
    id("org.springframework.boot")
    id("dev.daymor.ultimanexus.jvm.gradle.base.dependency-rules")
}

val config = UltimaNexusConfig.get(project)

val libs: VersionCatalog? = getLibsCatalogOrNull(project)

afterEvaluate {
    val isApp = config.applicationMode.get()

    if (isApp) {
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
        tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
            enabled = false
        }
        tasks.named<Jar>("jar") {
            enabled = true
            archiveClassifier.set("")
        }

        tasks.findByName("bootRun")?.enabled = false
    }
}

dependencies {
    compileOnly(libs?.let { getLibraryOrNull(it, "jspecify") } ?: Fallbacks.JSPECIFY)
    compileOnly(libs?.let { getLibraryOrNull(it, "jsr305") } ?: Fallbacks.JSR305)
    developmentOnly(libs?.let { getLibraryOrNull(it, "spring-boot-devtools") } ?: Fallbacks.SPRING_BOOT_DEVTOOLS)
}
