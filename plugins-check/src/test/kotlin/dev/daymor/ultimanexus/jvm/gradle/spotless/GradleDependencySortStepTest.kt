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

package dev.daymor.ultimanexus.jvm.gradle.spotless

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class GradleDependencySortStepTest {

    private val formatter = GradleDependencySortStep().toFormatter()

    private fun format(input: String): String = formatter.apply(input)

    @Nested
    inner class NoDependenciesBlock {

        @Test
        fun `returns input unchanged when no dependencies block exists`() {
            val input = """
                plugins {
                    id("java")
                }
            """.trimIndent()

            assertThat(format(input)).isEqualTo(input)
        }

        @Test
        fun `returns input unchanged when dependencies block is single line`() {
            val input = """
                dependencies { }
            """.trimIndent()

            assertThat(format(input)).isEqualTo(input)
        }

        @Test
        fun `returns input unchanged when dependencies block has content on same line`() {
            val input = """
                dependencies { implementation(libs.foo) }
            """.trimIndent()

            assertThat(format(input)).isEqualTo(input)
        }
    }

    @Nested
    inner class ScopeOrdering {

        @Test
        fun `sorts api dependencies before implementation`() {
            val input = """
                dependencies {
                    implementation(libs.bar)
                    api(libs.foo)
                }
            """.trimIndent()

            val expected = """
                dependencies {
                    api(libs.foo)
                    implementation(libs.bar)
                }
            """.trimIndent()

            assertThat(format(input)).isEqualTo(expected)
        }

        @Test
        fun `sorts implementation before compileOnly`() {
            val input = """
                dependencies {
                    compileOnly(libs.bar)
                    implementation(libs.foo)
                }
            """.trimIndent()

            val expected = """
                dependencies {
                    implementation(libs.foo)
                    compileOnly(libs.bar)
                }
            """.trimIndent()

            assertThat(format(input)).isEqualTo(expected)
        }

        @Test
        fun `sorts compileOnly before runtimeOnly`() {
            val input = """
                dependencies {
                    runtimeOnly(libs.bar)
                    compileOnly(libs.foo)
                }
            """.trimIndent()

            val expected = """
                dependencies {
                    compileOnly(libs.foo)
                    runtimeOnly(libs.bar)
                }
            """.trimIndent()

            assertThat(format(input)).isEqualTo(expected)
        }

        @Test
        fun `sorts annotationProcessor last within scope group`() {
            val input = """
                dependencies {
                    annotationProcessor(libs.lombok)
                    implementation(libs.foo)
                    api(libs.bar)
                }
            """.trimIndent()

            val expected = """
                dependencies {
                    api(libs.bar)
                    implementation(libs.foo)
                    annotationProcessor(libs.lombok)
                }
            """.trimIndent()

            assertThat(format(input)).isEqualTo(expected)
        }

        @Test
        fun `sorts compileOnlyApi before compileOnly`() {
            val input = """
                dependencies {
                    compileOnly(libs.bar)
                    compileOnlyApi(libs.foo)
                }
            """.trimIndent()

            val expected = """
                dependencies {
                    compileOnlyApi(libs.foo)
                    compileOnly(libs.bar)
                }
            """.trimIndent()

            assertThat(format(input)).isEqualTo(expected)
        }
    }

    @Nested
    inner class ProjectDependencies {

        @Test
        fun `project dependencies appear before library dependencies`() {
            val input = """
                dependencies {
                    implementation(libs.foo)
                    implementation(projects.core)
                }
            """.trimIndent()

            val expected = """
                dependencies {
                    implementation(projects.core)
                    implementation(libs.foo)
                }
            """.trimIndent()

            assertThat(format(input)).isEqualTo(expected)
        }

        @Test
        fun `project dependencies sorted before libraries within same scope`() {
            val input = """
                dependencies {
                    api(libs.bar)
                    api(projects.api)
                    implementation(libs.foo)
                    implementation(projects.core)
                }
            """.trimIndent()

            val expected = """
                dependencies {
                    api(projects.api)
                    api(libs.bar)
                    implementation(projects.core)
                    implementation(libs.foo)
                }
            """.trimIndent()

            assertThat(format(input)).isEqualTo(expected)
        }
    }

    @Nested
    inner class SourceSetGroups {

        @Test
        fun `groups dependencies by source set`() {
            val input = """
                dependencies {
                    testImplementation(libs.junit)
                    implementation(libs.guava)
                }
            """.trimIndent()

            val expected = """
                dependencies {
                    implementation(libs.guava)

                    testImplementation(libs.junit)
                }
            """.trimIndent()

            assertThat(format(input)).isEqualTo(expected)
        }

        @Test
        fun `inserts blank line between source set groups`() {
            val input = """
                dependencies {
                    integrationTestImplementation(libs.testcontainers)
                    testImplementation(libs.junit)
                    implementation(libs.guava)
                }
            """.trimIndent()

            val expected = """
                dependencies {
                    implementation(libs.guava)

                    integrationTestImplementation(libs.testcontainers)

                    testImplementation(libs.junit)
                }
            """.trimIndent()

            assertThat(format(input)).isEqualTo(expected)
        }
    }

    @Nested
    inner class MultilineDeclarations {

        @Test
        fun `handles multiline dependency declarations`() {
            val input = """
                dependencies {
                    implementation(libs.bar)
                    api(libs.foo
                        .withCapabilities())
                }
            """.trimIndent()

            val expected = """
                dependencies {
                    api(libs.foo.withCapabilities())
                    implementation(libs.bar)
                }
            """.trimIndent()

            assertThat(format(input)).isEqualTo(expected)
        }
    }

    @Nested
    inner class DependenciesConstraints {

        @Test
        fun `handles dependencies constraints block`() {
            val input = """
                dependencies.constraints {
                    implementation(libs.bar)
                    api(libs.foo)
                }
            """.trimIndent()

            val expected = """
                dependencies.constraints {
                    api(libs.foo)
                    implementation(libs.bar)
                }
            """.trimIndent()

            assertThat(format(input)).isEqualTo(expected)
        }
    }

    @Nested
    inner class FullScopeOrdering {

        @Test
        fun `maintains correct full scope ordering`() {
            val input = """
                dependencies {
                    annotationProcessor(libs.lombok)
                    runtimeOnly(libs.h2)
                    compileOnly(libs.servlet)
                    compileOnlyApi(libs.jspecify)
                    implementation(libs.guava)
                    api(libs.commons)
                }
            """.trimIndent()

            val expected = """
                dependencies {
                    api(libs.commons)
                    implementation(libs.guava)
                    compileOnlyApi(libs.jspecify)
                    compileOnly(libs.servlet)
                    runtimeOnly(libs.h2)
                    annotationProcessor(libs.lombok)
                }
            """.trimIndent()

            assertThat(format(input)).isEqualTo(expected)
        }
    }

    @Nested
    inner class Serialization {

        @Test
        fun `GradleDependencySortStep is serializable`() {
            val step = GradleDependencySortStep()

            assertThat(step).isInstanceOf(java.io.Serializable::class.java)
        }
    }
}
