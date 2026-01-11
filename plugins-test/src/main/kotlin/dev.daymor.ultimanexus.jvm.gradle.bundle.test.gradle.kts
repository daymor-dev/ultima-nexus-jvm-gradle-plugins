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

import dev.daymor.ultimanexus.jvm.gradle.util.DependencyUtils.FallbackVersions
import dev.daymor.ultimanexus.jvm.gradle.util.DependencyUtils.getLibsCatalogOrNull
import dev.daymor.ultimanexus.jvm.gradle.util.DependencyUtils.getVersionOrNull

/*
 * Test Bundle Plugin
 * ==================
 * Bundle plugin that applies unit tests and configurable test suites,
 * plus registers an "allTest" task to run all tests together.
 *
 * Includes:
 *   - dev.daymor.ultimanexus.jvm.gradle.test.test (unit tests)
 *   - dev.daymor.ultimanexus.jvm.gradle.test.test-suites (configurable suites)
 *
 * Default suites: integrationTest, functionalTest, performanceTest
 *
 * Configuration via extension (build.gradle.kts):
 * ```kotlin
 * testSuites {
 *     suites.set(listOf("integrationTest", "functionalTest", "smokeTest"))
 * }
 * ```
 *
 * Configuration via gradle.properties:
 * ```properties
 * test.suites=integrationTest,functionalTest,smokeTest
 * ```
 *
 * Usage:
 * ```kotlin
 * plugins {
 *     alias(libs.plugins.ultimanexus.jvm.bundle.test)
 * }
 * ```
 *
 * Run all tests: ./gradlew allTest
 */

plugins {
    java
    jacoco
    id("dev.daymor.ultimanexus.jvm.gradle.test.test")
    id("dev.daymor.ultimanexus.jvm.gradle.test.test-suites")
}

val libs: VersionCatalog? = getLibsCatalogOrNull(project)

val junitVersion = libs?.let { getVersionOrNull(it, "junit-jupiter") } ?: FallbackVersions.JUNIT_JUPITER

// Create allTest suite that depends on all other test suites (discovered dynamically)
afterEvaluate {
    testing.suites.register<JvmTestSuite>("allTest") {
        useJUnitJupiter(junitVersion)
        targets.named("allTest") {
            testTask {
                group = "verification"
                // Dynamically depend on all JvmTestSuites except "allTest" itself
                val allSuites = testing.suites.withType<JvmTestSuite>()
                    .filter { it.name != "allTest" }
                    .map { testing.suites.named(it.name) }
                dependsOn(allSuites)
            }
        }
    }

    dependencies {
        "allTestImplementation"(project)
    }
}
