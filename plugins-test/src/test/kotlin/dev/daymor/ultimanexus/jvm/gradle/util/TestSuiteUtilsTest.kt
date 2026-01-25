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
import org.assertj.core.api.Assertions.assertThat
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
}
