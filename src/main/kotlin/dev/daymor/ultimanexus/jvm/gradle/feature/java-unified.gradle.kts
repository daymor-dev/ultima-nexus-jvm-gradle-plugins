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

/**
 * Unified Java feature plugin with configurable application/library mode.
 *
 * Default: Application mode (application plugin applied).
 *
 * Configuration via build.gradle.kts:
 * ```kotlin
 * javaConfig {
 *     isApplication.set(false)  // library mode
 * }
 * ```
 *
 * Or via gradle.properties:
 * ```properties
 * java.isApplication=false
 * ```
 *
 * Usage:
 * plugins {
 *     id("dev.daymor.ultimanexus.jvm.gradle.feature.java-unified")
 * }
 */
plugins {
    java
    id("dev.daymor.ultimanexus.jvm.gradle.base.dependency-rules")
}

/**
 * Extension to configure Java mode.
 */
interface JavaConfigExtension {
    /**
     * Whether this is a Java application (true) or library (false).
     *
     * Application mode (default):
     * - Applies the `application` plugin
     * - Enables `run` task for execution
     * - Enables distribution tasks (distZip, distTar)
     * - Requires mainClass configuration
     *
     * Library mode:
     * - Applies the `java-library` plugin
     * - Uses api/implementation dependency separation
     * - No run or distribution tasks
     * - Designed for library consumers
     */
    val isApplication: Property<Boolean>
}

val javaConfig = extensions.create<JavaConfigExtension>("javaConfig")

// Read from gradle.properties with default true (application mode)
val isApplicationFromProps = providers.gradleProperty("java.isApplication").orNull?.toBoolean()
javaConfig.isApplication.convention(isApplicationFromProps ?: true)

// Configure after evaluation when extension values are finalized
afterEvaluate {
    val isApp = javaConfig.isApplication.get()

    if (isApp) {
        // Application mode: apply application plugin
        plugins.apply("application")
    } else {
        // Library mode: apply java-library plugin
        plugins.apply("java-library")
    }
}
