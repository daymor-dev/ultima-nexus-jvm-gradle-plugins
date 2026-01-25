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

package dev.daymor.ultimanexus.jvm.gradle

import dev.daymor.ultimanexus.jvm.gradle.config.PluginIds
import dev.daymor.ultimanexus.jvm.gradle.test.GradleProjectFixture
import dev.daymor.ultimanexus.jvm.gradle.test.taskSucceeded
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class PluginSmokeTest {

    @TempDir
    lateinit var tempDir: File

    private fun fixture() = GradleProjectFixture(tempDir)

    @Nested
    inner class LifecyclePlugin {

        @Test
        fun `lifecycle plugin applies successfully`() {
            fixture()
                .withSettings("rootProject.name = \"test-project\"")
                .withBuildScript(
                    """
                    plugins {
                        id("${PluginIds.Base.LIFECYCLE}")
                    }
                    """.trimIndent()
                )

            val result = fixture().help()

            assertThat(result.taskSucceeded(":help")).isTrue()
        }

        @Test
        fun `lifecycle plugin registers qualityCheck task`() {
            fixture()
                .withSettings("rootProject.name = \"test-project\"")
                .withBuildScript(
                    """
                    plugins {
                        id("${PluginIds.Base.LIFECYCLE}")
                    }
                    """.trimIndent()
                )

            val result = fixture().tasks()

            assertThat(result.output).contains("qualityCheck")
        }

        @Test
        fun `lifecycle plugin registers qualityGate task`() {
            fixture()
                .withSettings("rootProject.name = \"test-project\"")
                .withBuildScript(
                    """
                    plugins {
                        id("${PluginIds.Base.LIFECYCLE}")
                    }
                    """.trimIndent()
                )

            val result = fixture().tasks()

            assertThat(result.output).contains("qualityGate")
        }
    }

    @Nested
    inner class IdentityPlugin {

        @Test
        fun `identity plugin applies successfully with configured groupId`() {
            fixture()
                .withSettings("rootProject.name = \"test-project\"")
                .withBuildScript(
                    """
                    plugins {
                        id("${PluginIds.Base.IDENTITY}")
                    }
                    """.trimIndent()
                )
                .withProperties("groupId" to "com.example.test")

            val result = fixture().help()

            assertThat(result.taskSucceeded(":help")).isTrue()
        }

        @Test
        fun `identity plugin applies successfully with property configuration`() {
            fixture()
                .withSettings("rootProject.name = \"test-project\"")
                .withBuildScript(
                    """
                    plugins {
                        id("${PluginIds.Base.IDENTITY}")
                    }
                    """.trimIndent()
                )
                .withProperties("groupId" to "com.example.test")

            val result = fixture().help()

            assertThat(result.taskSucceeded(":help")).isTrue()
        }
    }

    @Nested
    inner class RepositoriesPlugin {

        @Test
        fun `repositories plugin applies successfully`() {
            fixture()
                .withSettings("rootProject.name = \"test-project\"")
                .withBuildScript(
                    """
                    plugins {
                        id("${PluginIds.Base.REPOSITORIES}")
                    }
                    """.trimIndent()
                )

            val result = fixture().help()

            assertThat(result.taskSucceeded(":help")).isTrue()
        }
    }

    @Nested
    inner class DependencyRulesPlugin {

        @Test
        fun `dependency-rules plugin applies successfully`() {
            fixture()
                .withSettings("rootProject.name = \"test-project\"")
                .withBuildScript(
                    """
                    plugins {
                        id("${PluginIds.Base.DEPENDENCY_RULES}")
                    }
                    """.trimIndent()
                )

            val result = fixture().help()

            assertThat(result.taskSucceeded(":help")).isTrue()
        }
    }

    @Nested
    inner class GradleProjectBundle {

        @Test
        fun `gradle-project bundle applies successfully`() {
            fixture()
                .withSettings("rootProject.name = \"test-project\"")
                .withBuildScript(
                    """
                    plugins {
                        id("${PluginIds.Bundle.GRADLE_PROJECT}")
                    }
                    """.trimIndent()
                )
                .withProperties("groupId" to "com.example.test")

            val result = fixture().help()

            assertThat(result.taskSucceeded(":help")).isTrue()
        }
    }
}
