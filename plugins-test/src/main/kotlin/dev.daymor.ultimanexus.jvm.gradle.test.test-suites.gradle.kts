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

import dev.daymor.ultimanexus.jvm.gradle.config.Defaults
import dev.daymor.ultimanexus.jvm.gradle.config.PropertyKeys
import dev.daymor.ultimanexus.jvm.gradle.util.DependencyUtils.Fallbacks
import dev.daymor.ultimanexus.jvm.gradle.util.DependencyUtils.getLibraryOrNull
import dev.daymor.ultimanexus.jvm.gradle.util.DependencyUtils.getLibsCatalogOrNull
import dev.daymor.ultimanexus.jvm.gradle.util.PropertyUtils.gradlePropertyAsBoolean
import dev.daymor.ultimanexus.jvm.gradle.util.PropertyUtils.gradlePropertyAsInt
import dev.daymor.ultimanexus.jvm.gradle.util.PropertyUtils.gradlePropertyOrNull

/*
 * Configurable Test Suites Plugin
 * ===============================
 * Creates multiple test suites from configuration. Supports both extension DSL
 * and gradle.properties configuration.
 *
 * Default suites: integrationTest, functionalTest, performanceTest
 *
 * Extension configuration (build.gradle.kts):
 * ```kotlin
 * testSuites {
 *     suites.set(listOf("integrationTest", "functionalTest", "smokeTest", "e2eTest"))
 *
 *     // Global defaults (inherited by all suites)
 *     maxHeapSize.set("2g")
 *     useByteBuddyAgent.set(true)
 *
 *     // Per-suite configuration
 *     suiteConfig("performanceTest") {
 *         useByteBuddyAgent.set(false)
 *     }
 *     suiteConfig("smokeTest") {
 *         maxParallelForks.set(1)
 *     }
 * }
 * ```
 *
 * Properties configuration (gradle.properties):
 * ```properties
 * test.suites=integrationTest,functionalTest,smokeTest,e2eTest
 * test.suite.performanceTest.useByteBuddyAgent=false
 * test.suite.smokeTest.maxParallelForks=1
 * ```
 *
 * Usage:
 * ```kotlin
 * plugins {
 *     alias(libs.plugins.ultimanexus.jvm.test.test.suites)
 * }
 * ```
 */

plugins {
    java
    `jvm-test-suite`
    jacoco
}

// Per-suite configuration interface
interface TestSuiteConfigSpec {
    val useByteBuddyAgent: Property<Boolean>
    val maxHeapSize: Property<String>
    val maxParallelForks: Property<Int>
    val showStandardStreams: Property<Boolean>
    val fileEncoding: Property<String>
}

// Main extension interface
interface TestSuitesExtension {
    val suites: ListProperty<String>
    val maxHeapSize: Property<String>
    val maxParallelForks: Property<Int>
    val showStandardStreams: Property<Boolean>
    val fileEncoding: Property<String>
    val useByteBuddyAgent: Property<Boolean>
    val suiteConfigs: NamedDomainObjectContainer<TestSuiteConfigSpec>

    fun suiteConfig(name: String, action: Action<TestSuiteConfigSpec>) {
        val config = suiteConfigs.maybeCreate(name)
        action.execute(config)
    }
}

val testSuitesExtension = extensions.create<TestSuitesExtension>("testSuites")

// Read global defaults from properties
val defaultParallelForks = (Runtime.getRuntime().availableProcessors() / 2).coerceAtLeast(1)

testSuitesExtension.maxHeapSize.convention(
    providers.gradlePropertyOrNull(PropertyKeys.Test.MAX_HEAP_SIZE) ?: Defaults.TEST_MAX_HEAP_SIZE
)
testSuitesExtension.maxParallelForks.convention(
    providers.gradlePropertyAsInt(PropertyKeys.Test.MAX_PARALLEL_FORKS, defaultParallelForks)
)
testSuitesExtension.showStandardStreams.convention(
    providers.gradlePropertyAsBoolean(PropertyKeys.Test.SHOW_STANDARD_STREAMS, true)
)
testSuitesExtension.fileEncoding.convention(
    providers.gradlePropertyOrNull(PropertyKeys.Test.FILE_ENCODING) ?: Defaults.TEST_FILE_ENCODING
)
testSuitesExtension.useByteBuddyAgent.convention(
    providers.gradlePropertyAsBoolean(PropertyKeys.Test.USE_BYTE_BUDDY_AGENT, true)
)

// Read suites from properties or use defaults
val suitesFromProps = providers.gradlePropertyOrNull(PropertyKeys.Test.SUITES)
    ?.split(",")
    ?.map { it.trim() }
    ?.filter { it.isNotBlank() }

testSuitesExtension.suites.convention(suitesFromProps ?: Defaults.DEFAULT_TEST_SUITES)

// ByteBuddy agent configuration
val byteBuddyAgent: Configuration =
    configurations.findByName(Defaults.ConfigurationName.BYTE_BUDDY_AGENT)
        ?: configurations.create(Defaults.ConfigurationName.BYTE_BUDDY_AGENT)

val libs: VersionCatalog? = getLibsCatalogOrNull(project)

dependencies {
    Defaults.ConfigurationName.BYTE_BUDDY_AGENT(
        libs?.let { getLibraryOrNull(it, "byte-buddy-agent") } ?: Fallbacks.BYTE_BUDDY_AGENT
    )
}

// Helper function to get per-suite property
fun getSuiteProperty(suiteName: String, property: String): String? =
    providers.gradlePropertyOrNull("${PropertyKeys.Test.SUITE_PREFIX}$suiteName.$property")

fun getSuiteBooleanProperty(suiteName: String, property: String, default: Boolean): Boolean =
    getSuiteProperty(suiteName, property)?.toBoolean() ?: default

fun getSuiteIntProperty(suiteName: String, property: String, default: Int): Int =
    getSuiteProperty(suiteName, property)?.toIntOrNull() ?: default

// Register test suites after evaluation to allow extension configuration
afterEvaluate {
    val suiteNames = testSuitesExtension.suites.get()

    suiteNames.forEach { suiteName ->
        // Get per-suite config if exists
        val suiteConfig = testSuitesExtension.suiteConfigs.findByName(suiteName)

        // Determine ByteBuddy usage: per-suite config > property > global default
        // Performance suites default to false
        val defaultByteBuddy = if (suiteName in Defaults.SUITES_WITHOUT_BYTEBUDDY) false
        else testSuitesExtension.useByteBuddyAgent.get()
        val useByteBuddy = suiteConfig?.useByteBuddyAgent?.orNull
            ?: getSuiteBooleanProperty(suiteName, "useByteBuddyAgent", defaultByteBuddy)

        val maxHeap = suiteConfig?.maxHeapSize?.orNull
            ?: getSuiteProperty(suiteName, "maxHeapSize")
            ?: testSuitesExtension.maxHeapSize.get()

        val parallelForks = suiteConfig?.maxParallelForks?.orNull
            ?: getSuiteIntProperty(suiteName, "maxParallelForks", testSuitesExtension.maxParallelForks.get())

        val showStreams = suiteConfig?.showStandardStreams?.orNull
            ?: getSuiteBooleanProperty(suiteName, "showStandardStreams", testSuitesExtension.showStandardStreams.get())

        val encoding = suiteConfig?.fileEncoding?.orNull
            ?: getSuiteProperty(suiteName, "fileEncoding")
            ?: testSuitesExtension.fileEncoding.get()

        // Register the test suite
        testing.suites.register<JvmTestSuite>(suiteName) {
            useJUnitJupiter()
            targets.named(suiteName) {
                testTask {
                    group = Defaults.TaskGroup.VERIFICATION
                    maxHeapSize = maxHeap
                    maxParallelForks = parallelForks
                    testLogging.showStandardStreams = showStreams
                    systemProperty("file.encoding", encoding)

                    if (useByteBuddy) {
                        jvmArgs = listOf(
                            "-javaagent:${byteBuddyAgent.singleFile.absolutePath}",
                            "-Xshare:off",
                        )
                    }
                }
            }
        }

        // Add implementation dependency on the project
        dependencies {
            "${suiteName}Implementation"(project)
        }
    }
}
