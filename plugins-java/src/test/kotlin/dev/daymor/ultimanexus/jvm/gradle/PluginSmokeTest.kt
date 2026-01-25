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
    inner class CompileJavaPlugin {

        @Test
        fun `compile-java plugin applies successfully`() {
            fixture()
                .withSettings("rootProject.name = \"test-project\"")
                .withBuildScript(
                    """
                    plugins {
                        id("${PluginIds.Feature.COMPILE_JAVA}")
                    }
                    """.trimIndent()
                )

            val result = fixture().help()

            assertThat(result.taskSucceeded(":help")).isTrue()
        }

        @Test
        fun `compile-java plugin configures via extension`() {
            fixture()
                .withSettings("rootProject.name = \"test-project\"")
                .withBuildScript(
                    """
                    plugins {
                        id("${PluginIds.Feature.COMPILE_JAVA}")
                    }

                    compileJavaConfig {
                        jdkVersion.set(21)
                    }
                    """.trimIndent()
                )

            val result = fixture().help()

            assertThat(result.taskSucceeded(":help")).isTrue()
        }

        @Test
        fun `compile-java plugin configures via property`() {
            fixture()
                .withSettings("rootProject.name = \"test-project\"")
                .withBuildScript(
                    """
                    plugins {
                        id("${PluginIds.Feature.COMPILE_JAVA}")
                    }
                    """.trimIndent()
                )
                .withProperties("jdkVersion" to "21")

            val result = fixture().help()

            assertThat(result.taskSucceeded(":help")).isTrue()
        }
    }

    @Nested
    inner class JavadocPlugin {

        @Test
        fun `javadoc plugin applies successfully`() {
            fixture()
                .withSettings("rootProject.name = \"test-project\"")
                .withBuildScript(
                    """
                    plugins {
                        java
                        id("${PluginIds.Feature.JAVADOC}")
                    }
                    """.trimIndent()
                )

            val result = fixture().help()

            assertThat(result.taskSucceeded(":help")).isTrue()
        }
    }

    @Nested
    inner class JspecifyPlugin {

        @Test
        fun `jspecify plugin applies successfully`() {
            fixture()
                .withSettings("rootProject.name = \"test-project\"")
                .withBuildScript(
                    """
                    plugins {
                        java
                        id("${PluginIds.Dependency.JSPECIFY}")
                    }
                    """.trimIndent()
                )

            val result = fixture().help()

            assertThat(result.taskSucceeded(":help")).isTrue()
        }
    }

    @Nested
    inner class JavaApplicationPlugin {

        @Test
        fun `java-application plugin applies successfully`() {
            fixture()
                .withSettings("rootProject.name = \"test-project\"")
                .withBuildScript(
                    """
                    plugins {
                        id("${PluginIds.Feature.JAVA_APPLICATION}")
                    }
                    """.trimIndent()
                )

            val result = fixture().help()

            assertThat(result.taskSucceeded(":help")).isTrue()
        }
    }

    @Nested
    inner class JavaLibraryPlugin {

        @Test
        fun `java-library plugin applies successfully`() {
            fixture()
                .withSettings("rootProject.name = \"test-project\"")
                .withBuildScript(
                    """
                    plugins {
                        id("${PluginIds.Feature.JAVA_LIBRARY}")
                    }
                    """.trimIndent()
                )

            val result = fixture().help()

            assertThat(result.taskSucceeded(":help")).isTrue()
        }
    }

    @Nested
    inner class JavaSimpleApplicationBundle {

        @Test
        fun `java-simple-application bundle applies successfully`() {
            fixture()
                .withSettings("rootProject.name = \"test-project\"")
                .withBuildScript(
                    """
                    plugins {
                        id("${PluginIds.Bundle.JAVA_SIMPLE_APPLICATION}")
                    }
                    """.trimIndent()
                )
                .withProperties("groupId" to "com.example.test")

            val result = fixture().help()

            assertThat(result.taskSucceeded(":help")).isTrue()
        }
    }

    @Nested
    inner class JavaSimpleLibraryBundle {

        @Test
        fun `java-simple-library bundle applies successfully`() {
            fixture()
                .withSettings("rootProject.name = \"test-project\"")
                .withBuildScript(
                    """
                    plugins {
                        id("${PluginIds.Bundle.JAVA_SIMPLE_LIBRARY}")
                    }
                    """.trimIndent()
                )
                .withProperties("groupId" to "com.example.test")

            val result = fixture().help()

            assertThat(result.taskSucceeded(":help")).isTrue()
        }
    }

    @Nested
    inner class JavaCompleteApplicationBundle {

        @Test
        fun `java-complete-application bundle applies successfully`() {
            fixture()
                .withSettings("rootProject.name = \"test-project\"")
                .withBuildScript(
                    """
                    plugins {
                        id("${PluginIds.Bundle.JAVA_COMPLETE_APPLICATION}")
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
    inner class JavaCompleteLibraryBundle {

        @Test
        fun `java-complete-library bundle applies successfully`() {
            fixture()
                .withSettings("rootProject.name = \"test-project\"")
                .withBuildScript(
                    """
                    plugins {
                        id("${PluginIds.Bundle.JAVA_COMPLETE_LIBRARY}")
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
