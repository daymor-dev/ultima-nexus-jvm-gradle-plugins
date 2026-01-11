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
 * Convention plugin for testing Gradle plugins with TestKit.
 *
 * Applies JUnit 5 with parameterized test support, Gradle TestKit,
 * and AssertJ for fluent assertions.
 *
 * Usage:
 * ```kotlin
 * plugins {
 *     alias(libs.plugins.ultimanexus.jvm.test.gradle.plugin)
 * }
 * ```
 */
plugins {
    java
}

val libs = getLibsCatalogOrNull(project)

dependencies {
    testImplementation(gradleTestKit())
    testImplementation(libs?.let { getLibraryOrNull(it, "junit-jupiter-params") } ?: Fallbacks.JUNIT_JUPITER_PARAMS)
    testImplementation(libs?.let { getLibraryOrNull(it, "assertj-core") } ?: Fallbacks.ASSERTJ_CORE)
}

testing.suites.named<JvmTestSuite>("test") {
    useJUnitJupiter()
}
