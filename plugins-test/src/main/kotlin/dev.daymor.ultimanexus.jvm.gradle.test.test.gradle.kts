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
import dev.daymor.ultimanexus.jvm.gradle.util.PropertyUtils.conventionFromProperty
import dev.daymor.ultimanexus.jvm.gradle.util.PropertyUtils.findPropertyAsInt
import dev.daymor.ultimanexus.jvm.gradle.util.PropertyUtils.findPropertyOrNull

/*
 * Unit Test Plugin
 * ================
 * Configures the default "test" suite with JUnit Jupiter, JaCoCo coverage,
 * and ByteBuddy agent for mocking support.
 *
 * Usage:
 *   plugins {
 *       id("dev.daymor.ultimanexus.jvm.gradle.test.test")
 *   }
 *
 * Extension configuration:
 *   testConfig {
 *       maxHeapSize.set("2g")
 *       maxParallelForks.set(4)
 *       showStandardStreams.set(false)
 *       fileEncoding.set("UTF-8")
 *       useByteBuddyAgent.set(true)
 *   }
 *
 * Properties configuration (gradle.properties):
 *   ultimanexus.test.maxHeapSize=2g
 *   ultimanexus.test.maxParallelForks=4
 *   ultimanexus.test.showStandardStreams=false
 *   ultimanexus.test.fileEncoding=UTF-8
 *   ultimanexus.test.useByteBuddyAgent=true
 */

plugins {
    java
    jacoco
}

interface TestConfigExtension {
    val maxHeapSize: Property<String>
    val maxParallelForks: Property<Int>
    val showStandardStreams: Property<Boolean>
    val fileEncoding: Property<String>
    val useByteBuddyAgent: Property<Boolean>
}

val testConfig = extensions.create<TestConfigExtension>("testConfig")

val defaultParallelForks = (Runtime.getRuntime().availableProcessors() / 2).coerceAtLeast(1)
testConfig.maxHeapSize.convention(project.findPropertyOrNull(PropertyKeys.Test.MAX_HEAP_SIZE) ?: Defaults.TEST_MAX_HEAP_SIZE)
testConfig.maxParallelForks.convention(project.findPropertyAsInt(PropertyKeys.Test.MAX_PARALLEL_FORKS, defaultParallelForks))
testConfig.showStandardStreams.conventionFromProperty(project, PropertyKeys.Test.SHOW_STANDARD_STREAMS, true)
testConfig.fileEncoding.convention(project.findPropertyOrNull(PropertyKeys.Test.FILE_ENCODING) ?: Defaults.FILE_ENCODING)
testConfig.useByteBuddyAgent.conventionFromProperty(project, PropertyKeys.Test.USE_BYTE_BUDDY_AGENT, true)

val byteBuddyAgent: Configuration =
    configurations.findByName(Defaults.ConfigurationName.BYTE_BUDDY_AGENT)
        ?: configurations.create(Defaults.ConfigurationName.BYTE_BUDDY_AGENT)

val libs: VersionCatalog? = getLibsCatalogOrNull(project)

dependencies {
    testRuntimeOnly(libs?.let { getLibraryOrNull(it, "junit-jupiter-engine") } ?: Fallbacks.JUNIT_JUPITER_ENGINE)
    testRuntimeOnly(libs?.let { getLibraryOrNull(it, "slf4j-simple") } ?: Fallbacks.SLF4J_SIMPLE)
    Defaults.ConfigurationName.BYTE_BUDDY_AGENT(libs?.let { getLibraryOrNull(it, "byte-buddy-agent") } ?: Fallbacks.BYTE_BUDDY_AGENT)
}

testing.suites.named<JvmTestSuite>("test") {
    useJUnitJupiter()
    targets.named("test") {
        testTask {
            group = Defaults.TaskGroup.VERIFICATION
            maxParallelForks = testConfig.maxParallelForks.get()
            testLogging.showStandardStreams = testConfig.showStandardStreams.get()
            maxHeapSize = testConfig.maxHeapSize.get()
            systemProperty("file.encoding", testConfig.fileEncoding.get())

            if (testConfig.useByteBuddyAgent.get()) {
                jvmArgumentProviders.add(
                    objects.newInstance<ByteBuddyAgentArgumentProvider>().apply {
                        agentClasspath.from(byteBuddyAgent)
                    }
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
