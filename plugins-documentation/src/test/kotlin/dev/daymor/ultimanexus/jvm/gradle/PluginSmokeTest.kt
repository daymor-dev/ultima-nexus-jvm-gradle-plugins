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
    inner class AntoraPlugin {

        @Test
        fun `antora plugin applies successfully`() {
            fixture()
                .withSettings("rootProject.name = \"test-project\"")
                .withBuildScript(
                    """
                    plugins {
                        id("${PluginIds.Feature.ANTORA}")
                    }
                    """.trimIndent()
                )

            val result = fixture().help()

            assertThat(result.taskSucceeded(":help")).isTrue()
        }
    }

    @Nested
    inner class AntoraUiPlugin {

        @Test
        fun `antora-ui plugin applies successfully`() {
            fixture()
                .withSettings("rootProject.name = \"test-project\"")
                .withBuildScript(
                    """
                    plugins {
                        id("${PluginIds.Feature.ANTORA_UI}")
                    }
                    """.trimIndent()
                )

            val result = fixture().help()

            assertThat(result.taskSucceeded(":help")).isTrue()
        }
    }

    @Nested
    inner class DocumentationBundle {

        @Test
        fun `documentation bundle applies successfully`() {
            fixture()
                .withSettings("rootProject.name = \"test-project\"")
                .withBuildScript(
                    """
                    plugins {
                        id("${PluginIds.Bundle.DOCUMENTATION}")
                    }
                    """.trimIndent()
                )

            val result = fixture().help()

            assertThat(result.taskSucceeded(":help")).isTrue()
        }
    }
}
