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

class ConventionPluginIntegrationTest {

    @TempDir
    lateinit var tempDir: File

    private fun fixture() = GradleProjectFixture(tempDir)

    @Nested
    inner class TestGradlePluginConvention {

        @Test
        fun `test gradle-plugin convention applies successfully`() {
            fixture()
                .withSettings("rootProject.name = \"test-project\"")
                .withBuildScript(
                    """
                    plugins {
                        `kotlin-dsl`
                        id("${PluginIds.Test.GRADLE_PLUGIN}")
                    }
                    """.trimIndent()
                )

            val result = fixture().help()

            assertThat(result.taskSucceeded(":help")).isTrue()
        }

        @Test
        fun `test gradle-plugin convention registers test task`() {
            fixture()
                .withSettings("rootProject.name = \"test-project\"")
                .withBuildScript(
                    """
                    plugins {
                        `kotlin-dsl`
                        id("${PluginIds.Test.GRADLE_PLUGIN}")
                    }
                    """.trimIndent()
                )

            val result = fixture().tasks()

            assertThat(result.output).contains("test -")
        }
    }

    @Nested
    inner class TestUnitConvention {

        @Test
        fun `test unit convention applies successfully`() {
            fixture()
                .withSettings("rootProject.name = \"test-project\"")
                .withBuildScript(
                    """
                    plugins {
                        `kotlin-dsl`
                        id("${PluginIds.Test.UNIT}")
                    }
                    """.trimIndent()
                )

            val result = fixture().help()

            assertThat(result.taskSucceeded(":help")).isTrue()
        }

        @Test
        fun `test unit convention registers test task`() {
            fixture()
                .withSettings("rootProject.name = \"test-project\"")
                .withBuildScript(
                    """
                    plugins {
                        `kotlin-dsl`
                        id("${PluginIds.Test.UNIT}")
                    }
                    """.trimIndent()
                )

            val result = fixture().tasks()

            assertThat(result.output).contains("test -")
        }
    }

    @Nested
    inner class GradleProjectFixtureTests {

        @Test
        fun `fixture can create and run a basic project`() {
            fixture()
                .withSettings("rootProject.name = \"basic-project\"")
                .withBuildScript(
                    """
                    plugins {
                        base
                    }
                    """.trimIndent()
                )

            val result = fixture().help()

            assertThat(result.taskSucceeded(":help")).isTrue()
        }

        @Test
        fun `fixture can create project with properties`() {
            fixture()
                .withSettings("rootProject.name = \"props-project\"")
                .withBuildScript(
                    """
                    plugins {
                        base
                    }

                    tasks.register("printVersion") {
                        doLast {
                            println("Version: ${'$'}{project.findProperty("myVersion")}")
                        }
                    }
                    """.trimIndent()
                )
                .withProperties("myVersion" to "1.2.3")

            val result = fixture().build("printVersion")

            assertThat(result.output).contains("Version: 1.2.3")
        }

        @Test
        fun `fixture can create project with source files`() {
            fixture()
                .withSettings("rootProject.name = \"source-project\"")
                .withBuildScript(
                    """
                    plugins {
                        java
                    }
                    """.trimIndent()
                )
                .withSourceFile(
                    "src/main/java/com/example/Hello.java",
                    """
                    package com.example;
                    public class Hello {
                        public static void main(String[] args) {
                            System.out.println("Hello");
                        }
                    }
                    """.trimIndent()
                )

            val result = fixture().build("compileJava")

            assertThat(result.taskSucceeded(":compileJava")).isTrue()
        }
    }
}
