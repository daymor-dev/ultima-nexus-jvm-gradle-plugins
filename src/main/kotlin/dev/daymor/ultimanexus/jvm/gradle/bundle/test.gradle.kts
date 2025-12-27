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

package dev.daymor.ultimanexus.jvm.gradle.bundle

import dev.daymor.ultimanexus.jvm.gradle.util.DependencyUtils.Fallbacks
import dev.daymor.ultimanexus.jvm.gradle.util.DependencyUtils.getVersionOrNull

/**
 * Bundle plugin for all test suites.
 * Applies unit, integration, functional, and performance test configurations.
 *
 * Usage:
 * plugins {
 *     id("dev.daymor.ultimanexus.jvm.gradle.bundle.test")
 * }
 */
plugins {
    java
    jacoco
    id("dev.daymor.ultimanexus.jvm.gradle.test.test")
    id("dev.daymor.ultimanexus.jvm.gradle.test.integration-test")
    id("dev.daymor.ultimanexus.jvm.gradle.test.functional-test")
    id("dev.daymor.ultimanexus.jvm.gradle.test.performance-test")
}

val libs: VersionCatalog = versionCatalogs.named("libs")

// Get JUnit version from catalog or use fallback
val junitVersion = getVersionOrNull(libs, "junit-jupiter") ?: Fallbacks.JUNIT_JUPITER_VERSION

testing.suites.register<JvmTestSuite>("allTest") {
    useJUnitJupiter(junitVersion)
    targets.named("allTest") {
        testTask {
            group = "verification"
            dependsOn(
                testing.suites.named("test"),
                testing.suites.named("integrationTest"),
                testing.suites.named("functionalTest"),
                testing.suites.named("performanceTest"),
            )
        }
    }
    dependencies { implementation(project()) }
}
