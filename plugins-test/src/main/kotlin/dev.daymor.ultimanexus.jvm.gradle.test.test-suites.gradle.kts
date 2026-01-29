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
import dev.daymor.ultimanexus.jvm.gradle.util.ByteBuddyAgentArgumentProvider
import dev.daymor.ultimanexus.jvm.gradle.util.DependencyUtils.Fallbacks
import dev.daymor.ultimanexus.jvm.gradle.util.DependencyUtils.getLibraryOrNull
import dev.daymor.ultimanexus.jvm.gradle.util.DependencyUtils.getLibsCatalogOrNull
import dev.daymor.ultimanexus.jvm.gradle.util.PropertyUtils.findPropertyAsBoolean
import dev.daymor.ultimanexus.jvm.gradle.util.PropertyUtils.findPropertyAsInt
import dev.daymor.ultimanexus.jvm.gradle.util.PropertyUtils.findPropertyOrNull

/**
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

interface TestSuiteConfigSpec {
    val useByteBuddyAgent: Property<Boolean>
    val maxHeapSize: Property<String>
    val maxParallelForks: Property<Int>
    val showStandardStreams: Property<Boolean>
    val fileEncoding: Property<String>
}

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

val defaultParallelForks = (Runtime.getRuntime().availableProcessors() / 2).coerceAtLeast(1)

testSuitesExtension.maxHeapSize.convention(
    project.findPropertyOrNull(PropertyKeys.Test.MAX_HEAP_SIZE) ?: Defaults.TEST_MAX_HEAP_SIZE
)
testSuitesExtension.maxParallelForks.convention(
    project.findPropertyAsInt(PropertyKeys.Test.MAX_PARALLEL_FORKS, defaultParallelForks)
)
testSuitesExtension.showStandardStreams.convention(
    project.findPropertyAsBoolean(PropertyKeys.Test.SHOW_STANDARD_STREAMS, true)
)
testSuitesExtension.fileEncoding.convention(
    project.findPropertyOrNull(PropertyKeys.Test.FILE_ENCODING) ?: Defaults.FILE_ENCODING
)
testSuitesExtension.useByteBuddyAgent.convention(
    project.findPropertyAsBoolean(PropertyKeys.Test.USE_BYTE_BUDDY_AGENT, true)
)

val suitesFromProps = project.findPropertyOrNull(PropertyKeys.Test.SUITES)
    ?.split(",")
    ?.map { it.trim() }
    ?.filter { it.isNotBlank() }

testSuitesExtension.suites.convention(suitesFromProps ?: Defaults.DEFAULT_TEST_SUITES)

val byteBuddyAgent: Configuration =
    configurations.findByName(Defaults.ConfigurationName.BYTE_BUDDY_AGENT)
        ?: configurations.create(Defaults.ConfigurationName.BYTE_BUDDY_AGENT)

val libs: VersionCatalog? = getLibsCatalogOrNull(project)

dependencies {
    Defaults.ConfigurationName.BYTE_BUDDY_AGENT(
        libs?.let { getLibraryOrNull(it, "byte-buddy-agent") } ?: Fallbacks.BYTE_BUDDY_AGENT
    )
}

fun getSuiteProperty(suiteName: String, property: String): String? =
    project.findPropertyOrNull("${PropertyKeys.Test.SUITE_PREFIX}$suiteName.$property")

fun getSuiteBooleanProperty(suiteName: String, property: String, default: Boolean): Boolean =
    getSuiteProperty(suiteName, property)?.toBoolean() ?: default

fun getSuiteIntProperty(suiteName: String, property: String, default: Int): Int =
    getSuiteProperty(suiteName, property)?.toIntOrNull() ?: default

val suiteNames = testSuitesExtension.suites.get()

suiteNames.forEach { suiteName ->
    val suiteConfig = testSuitesExtension.suiteConfigs.findByName(suiteName)

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
                    jvmArgumentProviders.add(
                        objects.newInstance<ByteBuddyAgentArgumentProvider>().apply {
                            agentClasspath.from(byteBuddyAgent)
                        }
                    )
                }
            }
        }
    }

    dependencies {
        "${suiteName}Implementation"(project)
    }

    configurations.named("${suiteName}Implementation") {
        extendsFrom(configurations.getByName("testImplementation"))
    }
}
