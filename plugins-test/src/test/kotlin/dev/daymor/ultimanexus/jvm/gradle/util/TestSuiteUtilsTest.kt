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

package dev.daymor.ultimanexus.jvm.gradle.util

import dev.daymor.ultimanexus.jvm.gradle.config.Defaults
import dev.daymor.ultimanexus.jvm.gradle.config.PropertyKeys
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.tasks.testing.Test as GradleTest
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class TestSuiteUtilsTest {

    @TempDir
    lateinit var tempDir: File

    @Nested
    inner class GetOrCreateByteBuddyAgentConfiguration {

        @Test
        fun `creates configuration when not exists`() {
            val project = ProjectBuilder.builder().withProjectDir(tempDir).build()

            val config = TestSuiteUtils.getOrCreateByteBuddyAgentConfiguration(project)

            assertThat(config).isNotNull
            assertThat(config.name).isEqualTo(Defaults.ConfigurationName.BYTE_BUDDY_AGENT)
        }

        @Test
        fun `returns existing configuration when present`() {
            val project = ProjectBuilder.builder().withProjectDir(tempDir).build()
            val existing = project.configurations.create(Defaults.ConfigurationName.BYTE_BUDDY_AGENT)

            val config = TestSuiteUtils.getOrCreateByteBuddyAgentConfiguration(project)

            assertThat(config).isSameAs(existing)
        }

        @Test
        fun `configuration is reusable across multiple calls`() {
            val project = ProjectBuilder.builder().withProjectDir(tempDir).build()

            val config1 = TestSuiteUtils.getOrCreateByteBuddyAgentConfiguration(project)
            val config2 = TestSuiteUtils.getOrCreateByteBuddyAgentConfiguration(project)

            assertThat(config1).isSameAs(config2)
        }
    }

    @Nested
    inner class ConfigureTestTask {

        private fun createTestTask(): GradleTest {
            val project = ProjectBuilder.builder().withProjectDir(tempDir).build()
            project.plugins.apply("java")
            return project.tasks.named("test", GradleTest::class.java).get()
        }

        private fun mockProviders(vararg properties: Pair<String, String?>): ProviderFactory {
            val providers = mockk<ProviderFactory>()
            val propertyMap = mapOf(
                PropertyKeys.Test.MAX_HEAP_SIZE to null,
                PropertyKeys.Test.MAX_PARALLEL_FORKS to null,
                PropertyKeys.Test.SHOW_STANDARD_STREAMS to null,
                PropertyKeys.Test.FILE_ENCODING to null,
                PropertyKeys.Test.USE_BYTE_BUDDY_AGENT to null
            ) + properties.toMap()

            propertyMap.forEach { (key, value) ->
                val provider = mockk<Provider<String>>()
                every { providers.gradleProperty(key) } returns provider
                every { provider.orNull } returns value
            }
            return providers
        }

        @Test
        fun `configures max heap size from property`() {
            val testTask = createTestTask()
            val providers = mockProviders(PropertyKeys.Test.MAX_HEAP_SIZE to "4g")

            TestSuiteUtils.configureTestTask(testTask, null, providers, useByteBuddy = false)

            assertThat(testTask.maxHeapSize).isEqualTo("4g")
        }

        @Test
        fun `uses default max heap size when property not set`() {
            val testTask = createTestTask()
            val providers = mockProviders()

            TestSuiteUtils.configureTestTask(testTask, null, providers, useByteBuddy = false)

            assertThat(testTask.maxHeapSize).isEqualTo(Defaults.TEST_MAX_HEAP_SIZE)
        }

        @Test
        fun `sets task group to verification`() {
            val testTask = createTestTask()
            val providers = mockProviders()

            TestSuiteUtils.configureTestTask(testTask, null, providers, useByteBuddy = false)

            assertThat(testTask.group).isEqualTo(Defaults.TaskGroup.VERIFICATION)
        }

        @Test
        fun `configures file encoding system property`() {
            val testTask = createTestTask()
            val providers = mockProviders(PropertyKeys.Test.FILE_ENCODING to "ISO-8859-1")

            TestSuiteUtils.configureTestTask(testTask, null, providers, useByteBuddy = false)

            assertThat(testTask.allJvmArgs.toString()).contains("-Dfile.encoding=ISO-8859-1")
        }

        @Test
        fun `uses default file encoding when property not set`() {
            val testTask = createTestTask()
            val providers = mockProviders()

            TestSuiteUtils.configureTestTask(testTask, null, providers, useByteBuddy = false)

            assertThat(testTask.allJvmArgs.toString()).contains("-Dfile.encoding=${Defaults.TEST_FILE_ENCODING}")
        }

        @Test
        fun `calculates parallel forks from available processors`() {
            val testTask = createTestTask()
            val providers = mockProviders()

            TestSuiteUtils.configureTestTask(testTask, null, providers, useByteBuddy = false)

            val expectedForks = (Runtime.getRuntime().availableProcessors() / 2).coerceAtLeast(1)
            assertThat(testTask.maxParallelForks).isEqualTo(expectedForks)
        }

        @Test
        fun `configures parallel forks from property`() {
            val testTask = createTestTask()
            val providers = mockProviders(PropertyKeys.Test.MAX_PARALLEL_FORKS to "8")

            TestSuiteUtils.configureTestTask(testTask, null, providers, useByteBuddy = false)

            assertThat(testTask.maxParallelForks).isEqualTo(8)
        }
    }
}
