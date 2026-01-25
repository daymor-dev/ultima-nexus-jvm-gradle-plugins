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

/*
 * Plugin: dev.daymor.ultimanexus.jvm.gradle.bundle.spring-boot-simple-application
 *
 * Bundle plugin for simple Spring Boot application development.
 * Applies base Java conventions with Spring Boot application support.
 * Does not include quality checks, reporting, or publishing.
 *
 * For standalone Spring Boot applications without full tooling.
 *
 * Usage:
 * ```kotlin
 * plugins {
 *     alias(libs.plugins.ultimanexus.bundle.spring.boot.simple.application)
 * }
 * ```
 */
plugins {
    id("dev.daymor.ultimanexus.jvm.gradle.bundle.gradle-project")
    id("dev.daymor.ultimanexus.jvm.gradle.feature.compile-java")
    id("dev.daymor.ultimanexus.jvm.gradle.feature.spring-boot-application")
    id("dev.daymor.ultimanexus.jvm.gradle.dependency.spring-boot-test")
}
