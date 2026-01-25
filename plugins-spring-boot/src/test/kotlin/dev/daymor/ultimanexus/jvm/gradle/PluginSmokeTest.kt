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
    inner class SpringBootApplicationPlugin {

        @Test
        fun `spring-boot-application plugin applies successfully`() {
            fixture()
                .withSettings("rootProject.name = \"test-project\"")
                .withBuildScript(
                    """
                    plugins {
                        id("${PluginIds.Feature.SPRING_BOOT_APPLICATION}")
                    }
                    """.trimIndent()
                )

            val result = fixture().help()

            assertThat(result.taskSucceeded(":help")).isTrue()
        }
    }

    @Nested
    inner class SpringBootLibraryPlugin {

        @Test
        fun `spring-boot-library plugin applies successfully`() {
            fixture()
                .withSettings("rootProject.name = \"test-project\"")
                .withBuildScript(
                    """
                    plugins {
                        id("${PluginIds.Feature.SPRING_BOOT_LIBRARY}")
                    }
                    """.trimIndent()
                )

            val result = fixture().help()

            assertThat(result.taskSucceeded(":help")).isTrue()
        }
    }

    @Nested
    inner class SpringBootTestPlugin {

        @Test
        fun `spring-boot-test plugin applies successfully`() {
            fixture()
                .withSettings("rootProject.name = \"test-project\"")
                .withBuildScript(
                    """
                    plugins {
                        java
                        id("${PluginIds.Dependency.SPRING_BOOT_TEST}")
                    }
                    """.trimIndent()
                )

            val result = fixture().help()

            assertThat(result.taskSucceeded(":help")).isTrue()
        }
    }

    @Nested
    inner class SpringBootSimpleApplicationBundle {

        @Test
        fun `spring-boot-simple-application bundle applies successfully`() {
            fixture()
                .withSettings("rootProject.name = \"test-project\"")
                .withBuildScript(
                    """
                    plugins {
                        id("${PluginIds.Bundle.SPRING_BOOT_SIMPLE_APPLICATION}")
                    }
                    """.trimIndent()
                )
                .withProperties("groupId" to "com.example.test")

            val result = fixture().help()

            assertThat(result.taskSucceeded(":help")).isTrue()
        }
    }

    @Nested
    inner class SpringBootSimpleLibraryBundle {

        @Test
        fun `spring-boot-simple-library bundle applies successfully`() {
            fixture()
                .withSettings("rootProject.name = \"test-project\"")
                .withBuildScript(
                    """
                    plugins {
                        id("${PluginIds.Bundle.SPRING_BOOT_SIMPLE_LIBRARY}")
                    }
                    """.trimIndent()
                )
                .withProperties("groupId" to "com.example.test")

            val result = fixture().help()

            assertThat(result.taskSucceeded(":help")).isTrue()
        }
    }

    @Nested
    inner class SpringBootCompleteApplicationBundle {

        @Test
        fun `spring-boot-complete-application bundle applies successfully`() {
            fixture()
                .withSettings("rootProject.name = \"test-project\"")
                .withBuildScript(
                    """
                    plugins {
                        id("${PluginIds.Bundle.SPRING_BOOT_COMPLETE_APPLICATION}")
                    }
                    """.trimIndent()
                )
                .withProperties(
                    "groupId" to "com.example.test",
                    "checkArtifactName" to "my-check-artifact"
                )
                .withVersionCatalog(
                    """
                    [libraries]
                    my-check-artifact = "org.example:check-artifact:1.0.0"
                    """.trimIndent()
                )

            val result = fixture().help()

            assertThat(result.taskSucceeded(":help")).isTrue()
        }
    }

    @Nested
    inner class SpringBootCompleteLibraryBundle {

        @Test
        fun `spring-boot-complete-library bundle applies successfully`() {
            fixture()
                .withSettings("rootProject.name = \"test-project\"")
                .withBuildScript(
                    """
                    plugins {
                        id("${PluginIds.Bundle.SPRING_BOOT_COMPLETE_LIBRARY}")
                    }
                    """.trimIndent()
                )
                .withProperties(
                    "groupId" to "com.example.test",
                    "checkArtifactName" to "my-check-artifact"
                )
                .withVersionCatalog(
                    """
                    [libraries]
                    my-check-artifact = "org.example:check-artifact:1.0.0"
                    """.trimIndent()
                )

            val result = fixture().help()

            assertThat(result.taskSucceeded(":help")).isTrue()
        }
    }
}
