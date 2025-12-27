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

package dev.daymor.ultimanexus.jvm.gradle.test

import dev.daymor.ultimanexus.jvm.gradle.util.DependencyUtils.Fallbacks
import dev.daymor.ultimanexus.jvm.gradle.util.DependencyUtils.getLibraryOrNull

plugins {
    java
    jacoco
}

/**
 * Extension to configure unit test settings.
 */
interface TestConfigExtension {
    /** Maximum heap size for tests. Default: "1g" */
    val maxHeapSize: Property<String>
    /** Maximum parallel test forks. Default: half of available processors (minimum 1) */
    val maxParallelForks: Property<Int>
    /** Whether to show standard streams in test output. Default: true */
    val showStandardStreams: Property<Boolean>
    /** File encoding for tests. Default: "UTF-8" */
    val fileEncoding: Property<String>
    /** Whether to use ByteBuddy agent for mocking. Default: true */
    val useByteBuddyAgent: Property<Boolean>
}

val testConfig = extensions.create<TestConfigExtension>("testConfig")

// Read from gradle.properties with defaults
val defaultParallelForks = (Runtime.getRuntime().availableProcessors() / 2).coerceAtLeast(1)
val maxHeapSizeFromProps = providers.gradleProperty("test.maxHeapSize").orNull ?: "1g"
val maxParallelForksFromProps = providers.gradleProperty("test.maxParallelForks").orNull?.toInt() ?: defaultParallelForks
val showStandardStreamsFromProps = providers.gradleProperty("test.showStandardStreams").orNull?.toBoolean() ?: true
val fileEncodingFromProps = providers.gradleProperty("test.fileEncoding").orNull ?: "UTF-8"
val useByteBuddyAgentFromProps = providers.gradleProperty("test.useByteBuddyAgent").orNull?.toBoolean() ?: true

// Set defaults
testConfig.maxHeapSize.convention(maxHeapSizeFromProps)
testConfig.maxParallelForks.convention(maxParallelForksFromProps)
testConfig.showStandardStreams.convention(showStandardStreamsFromProps)
testConfig.fileEncoding.convention(fileEncodingFromProps)
testConfig.useByteBuddyAgent.convention(useByteBuddyAgentFromProps)

val byteBuddyAgent: Configuration =
    configurations.findByName("byteBuddyAgent")
        ?: configurations.create("byteBuddyAgent")

val libs: VersionCatalog = versionCatalogs.named("libs")

dependencies {
    testRuntimeOnly(getLibraryOrNull(libs, "junit-jupiter-engine") ?: Fallbacks.JUNIT_JUPITER_ENGINE)
    testRuntimeOnly(getLibraryOrNull(libs, "slf4j-simple") ?: Fallbacks.SLF4J_SIMPLE)
    "byteBuddyAgent"(getLibraryOrNull(libs, "byte-buddy-agent") ?: Fallbacks.BYTE_BUDDY_AGENT)
}

testing.suites.named<JvmTestSuite>("test") {
    useJUnitJupiter()
    targets.named("test") {
        testTask {
            group = "verification"
            maxParallelForks = testConfig.maxParallelForks.get()
            testLogging.showStandardStreams = testConfig.showStandardStreams.get()
            maxHeapSize = testConfig.maxHeapSize.get()
            systemProperty("file.encoding", testConfig.fileEncoding.get())

            if (testConfig.useByteBuddyAgent.get()) {
                jvmArgs = listOf(
                    "-javaagent:${byteBuddyAgent.singleFile.absolutePath}",
                    "-Xshare:off",
                )
            }
        }
    }
}

configurations.testRuntimeClasspath {
    resolutionStrategy.capabilitiesResolution {
        withCapability("org.gradlex:slf4j-impl:1.0") {
            candidates.removeIf {
                it.id.displayName.contains("org.slf4j:slf4j-simple")
            }
            if (candidates.isNotEmpty()) {
                select(candidates.first().id.displayName)
            }
        }
    }
}
