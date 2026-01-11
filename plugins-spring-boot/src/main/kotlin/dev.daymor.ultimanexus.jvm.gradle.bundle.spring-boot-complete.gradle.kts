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


import dev.daymor.ultimanexus.jvm.gradle.config.PluginIds
import dev.daymor.ultimanexus.jvm.gradle.config.UltimaNexusConfig

/*
 * Bundle plugin for complete Spring Boot development.
 * Supports both application and library modes via ultimaNexus extension.
 *
 * Publishing is conditionally included based on the application mode:
 * - Application mode (default): Does NOT include publish-java plugin
 * - Library mode: Includes publish-java plugin for Maven publishing
 *
 * Extension configuration (build.gradle.kts):
 *
 *     ultimaNexus {
 *         applicationMode.set(false)  // Library mode, enables publishing
 *     }
 *
 * Properties configuration (gradle.properties):
 *
 *     springBoot.isApplication=false
 *
 * Includes:
 * - dev.daymor.ultimanexus.jvm.gradle.bundle.gradle-project
 * - dev.daymor.ultimanexus.jvm.gradle.bundle.check (Checkstyle, PMD, SpotBugs, Spotless)
 * - dev.daymor.ultimanexus.jvm.gradle.bundle.test (unit, integration, functional, performance)
 * - dev.daymor.ultimanexus.jvm.gradle.bundle.report (code coverage, SBOM)
 * - dev.daymor.ultimanexus.jvm.gradle.feature.compile-java
 * - dev.daymor.ultimanexus.jvm.gradle.feature.javadoc
 * - dev.daymor.ultimanexus.jvm.gradle.feature.publish-java (only in library mode)
 * - dev.daymor.ultimanexus.jvm.gradle.feature.spring-boot-unified
 * - dev.daymor.ultimanexus.jvm.gradle.dependency.jspecify
 * - dev.daymor.ultimanexus.jvm.gradle.dependency.spring-boot-test
 *
 * Usage:
 * ```kotlin
 * plugins {
 *     alias(libs.plugins.ultimanexus.jvm.bundle.spring.boot.complete)
 * }
 * ```
 */

plugins {
    id("dev.daymor.ultimanexus.jvm.gradle.bundle.gradle-project")
    id("dev.daymor.ultimanexus.jvm.gradle.bundle.check")
    id("dev.daymor.ultimanexus.jvm.gradle.bundle.test")
    id("dev.daymor.ultimanexus.jvm.gradle.bundle.report")
    id("dev.daymor.ultimanexus.jvm.gradle.feature.compile-java")
    id("dev.daymor.ultimanexus.jvm.gradle.feature.javadoc")
    id("dev.daymor.ultimanexus.jvm.gradle.feature.spring-boot-unified")
    id("dev.daymor.ultimanexus.jvm.gradle.dependency.jspecify")
    id("dev.daymor.ultimanexus.jvm.gradle.dependency.spring-boot-test")
}

val config = UltimaNexusConfig.get(project)

afterEvaluate {
    val isLibrary = !config.applicationMode.get()
    if (isLibrary) {
        apply(plugin = PluginIds.Feature.PUBLISH_JAVA)
    }
}
