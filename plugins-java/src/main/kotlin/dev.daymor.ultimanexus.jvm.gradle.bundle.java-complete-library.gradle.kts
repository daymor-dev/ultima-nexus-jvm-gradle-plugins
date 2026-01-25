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
 * Plugin: dev.daymor.ultimanexus.jvm.gradle.bundle.java-complete-library
 *
 * Bundle plugin for complete Java library development.
 * Includes quality checks, testing, reporting, Javadoc, JSpecify annotations,
 * and Maven publishing support.
 *
 * For shared libraries, SDKs, or any reusable Java modules with full tooling.
 *
 * Usage:
 * ```kotlin
 * plugins {
 *     alias(libs.plugins.ultimanexus.bundle.java.complete.library)
 * }
 * ```
 */
plugins {
    id("dev.daymor.ultimanexus.jvm.gradle.bundle.gradle-project")
    id("dev.daymor.ultimanexus.jvm.gradle.feature.compile-java")
    id("dev.daymor.ultimanexus.jvm.gradle.feature.java-library")
    id("dev.daymor.ultimanexus.jvm.gradle.bundle.check")
    id("dev.daymor.ultimanexus.jvm.gradle.bundle.test")
    id("dev.daymor.ultimanexus.jvm.gradle.bundle.report")
    id("dev.daymor.ultimanexus.jvm.gradle.feature.javadoc")
    id("dev.daymor.ultimanexus.jvm.gradle.dependency.jspecify")
    id("dev.daymor.ultimanexus.jvm.gradle.feature.publish-java")
}
