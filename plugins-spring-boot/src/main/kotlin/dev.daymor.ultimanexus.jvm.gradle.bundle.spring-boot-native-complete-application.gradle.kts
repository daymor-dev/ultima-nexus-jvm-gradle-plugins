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

/**
 * Plugin: dev.daymor.ultimanexus.jvm.gradle.bundle.spring-boot-native-complete-application
 *
 * Bundle plugin for complete Spring Boot native application development.
 * Includes quality checks, testing, reporting, Javadoc, JSpecify annotations,
 * and GraalVM Native Image compilation.
 *
 * Includes:
 * - All features from spring-boot-complete-application
 * - GraalVM Native Image compilation (nativeCompile, nativeRun)
 * - Spring Boot AOT compilation settings
 *
 * Usage:
 * ```kotlin
 * plugins {
 *     alias(libs.plugins.ultimanexus.jvm.bundle.spring.boot.native.complete.application)
 * }
 * ```
 */
plugins {
    id("dev.daymor.ultimanexus.jvm.gradle.bundle.spring-boot-complete-application")
    id("dev.daymor.ultimanexus.jvm.gradle.feature.graalvm-native")
    id("dev.daymor.ultimanexus.jvm.gradle.feature.spring-boot-aot")
}
