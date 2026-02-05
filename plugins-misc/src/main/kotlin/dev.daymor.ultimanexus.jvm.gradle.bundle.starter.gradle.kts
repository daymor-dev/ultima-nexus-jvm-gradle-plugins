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
 * Plugin: dev.daymor.ultimanexus.jvm.gradle.bundle.starter
 *
 * Bundle plugin for starter subprojects that aggregate dependencies.
 *
 * Included plugins:
 *   - java-library
 *   - dev.daymor.ultimanexus.jvm.gradle.base.lifecycle
 *   - dev.daymor.ultimanexus.jvm.gradle.base.identity
 *   - dev.daymor.ultimanexus.jvm.gradle.base.dependency-rules
 *   - dev.daymor.ultimanexus.jvm.gradle.feature.publish
 *
 * Usage:
 * ```kotlin
 * // my-starter-web/build.gradle.kts
 * plugins {
 *     alias(libs.plugins.ultimanexus.jvm.bundle.starter)
 * }
 *
 * dependencies {
 *     api(libs.spring.boot.starter.web)
 *     api(libs.spring.boot.starter.validation)
 *     api(project(":my-common-utils"))
 * }
 * ```
 */
plugins {
    `java-library`
    id("dev.daymor.ultimanexus.jvm.gradle.base.lifecycle")
    id("dev.daymor.ultimanexus.jvm.gradle.base.identity")
    id("dev.daymor.ultimanexus.jvm.gradle.base.dependency-rules")
    id("dev.daymor.ultimanexus.jvm.gradle.feature.publish")
}
