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

package dev.daymor.ultimanexus.jvm.gradle.task

import org.assertj.core.api.Assertions.assertThat
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class PluginApplicationOrderAnalysisTest {

    @TempDir
    lateinit var tempDir: File

    private fun createTask(): PluginApplicationOrderAnalysis {
        val project = ProjectBuilder.builder().withProjectDir(tempDir).build()
        return project.tasks.register("analysePluginOrder", PluginApplicationOrderAnalysis::class.java).get()
    }

    @Nested
    inner class GradleFileFinding {

        @Test
        fun `finds gradle kts files in directory`() {
            val srcFolder = File(tempDir, "src/main/kotlin").apply { mkdirs() }
            File(srcFolder, "dev/test").mkdirs()
            File(srcFolder, "dev/test/test.gradle.kts").writeText("plugins { }")
            File(srcFolder, "dev/test/other.gradle.kts").writeText("plugins { }")

            val task = createTask()
            task.pluginSrcFolders.from(srcFolder)
            task.pluginApplicationDiagram.set(File(tempDir, "diagram.puml"))

            task.analyse()

            assertThat(task.pluginApplicationDiagram.get().asFile).exists()
        }

        @Test
        fun `finds gradle kts files recursively`() {
            val srcFolder = File(tempDir, "src/main/kotlin").apply { mkdirs() }
            File(srcFolder, "base").mkdirs()
            File(srcFolder, "base/core.gradle.kts").writeText("plugins { }")
            File(srcFolder, "feature").mkdirs()
            File(srcFolder, "feature/web.gradle.kts").writeText("plugins { }")

            val task = createTask()
            task.pluginSrcFolders.from(srcFolder)
            task.pluginApplicationDiagram.set(File(tempDir, "diagram.puml"))

            task.analyse()

            val content = task.pluginApplicationDiagram.get().asFile.readText()
            assertThat(content).contains("@startuml")
            assertThat(content).contains("@enduml")
        }
    }

    @Nested
    inner class PluginIdExtraction {

        @Test
        fun `extracts plugin id from id function call`() {
            val srcFolder = File(tempDir, "src/main/kotlin/dev/test").apply { mkdirs() }
            File(srcFolder, "my-plugin.gradle.kts").writeText(
                """
                plugins {
                    id("dev.example.base")
                }
                """.trimIndent()
            )

            val task = createTask()
            task.pluginSrcFolders.from(File(tempDir, "src/main/kotlin"))
            task.pluginApplicationDiagram.set(File(tempDir, "diagram.puml"))

            task.analyse()

            val content = task.pluginApplicationDiagram.get().asFile.readText()
            assertThat(content).contains("dev.test.my-plugin")
            assertThat(content).contains("dev.example.base")
        }

        @Test
        fun `handles multiple plugin declarations`() {
            val srcFolder = File(tempDir, "src/main/kotlin/dev/test").apply { mkdirs() }
            File(srcFolder, "bundle.gradle.kts").writeText(
                """
                plugins {
                    id("dev.example.one")
                    id("dev.example.two")
                }
                """.trimIndent()
            )

            val task = createTask()
            task.pluginSrcFolders.from(File(tempDir, "src/main/kotlin"))
            task.pluginApplicationDiagram.set(File(tempDir, "diagram.puml"))

            task.analyse()

            val content = task.pluginApplicationDiagram.get().asFile.readText()
            assertThat(content).contains("dev.example.one")
            assertThat(content).contains("dev.example.two")
        }

        @Test
        fun `returns empty list for file without plugins block`() {
            val srcFolder = File(tempDir, "src/main/kotlin/dev/test").apply { mkdirs() }
            File(srcFolder, "no-plugins.gradle.kts").writeText(
                """
                dependencies {
                    implementation(libs.foo)
                }
                """.trimIndent()
            )

            val task = createTask()
            task.pluginSrcFolders.from(File(tempDir, "src/main/kotlin"))
            task.pluginApplicationDiagram.set(File(tempDir, "diagram.puml"))

            task.analyse()

            val content = task.pluginApplicationDiagram.get().asFile.readText()
            assertThat(content).contains("@startuml")
            // File without id("...") calls in plugins block should still create the agent but with no dependencies
            assertThat(content).contains("dev.test.no-plugins")
        }
    }

    @Nested
    inner class PackagePathExtraction {

        @Test
        fun `extracts package path from relative file location`() {
            val srcFolder = File(tempDir, "src/main/kotlin/dev/example/feature").apply { mkdirs() }
            File(srcFolder, "web.gradle.kts").writeText(
                """
                plugins {
                    id("dev.base")
                }
                """.trimIndent()
            )

            val task = createTask()
            task.pluginSrcFolders.from(File(tempDir, "src/main/kotlin"))
            task.pluginApplicationDiagram.set(File(tempDir, "diagram.puml"))

            task.analyse()

            val content = task.pluginApplicationDiagram.get().asFile.readText()
            assertThat(content).contains("dev.example.feature")
            assertThat(content).contains("dev.example.feature.web")
        }
    }

    @Nested
    inner class PlantUmlGeneration {

        @Test
        fun `generates valid plantuml syntax`() {
            val srcFolder = File(tempDir, "src/main/kotlin/dev/test").apply { mkdirs() }
            File(srcFolder, "plugin-a.gradle.kts").writeText(
                """
                plugins {
                    id("dev.test.plugin-b")
                }
                """.trimIndent()
            )
            File(srcFolder, "plugin-b.gradle.kts").writeText("plugins { }")

            val task = createTask()
            task.pluginSrcFolders.from(File(tempDir, "src/main/kotlin"))
            task.pluginApplicationDiagram.set(File(tempDir, "diagram.puml"))

            task.analyse()

            val content = task.pluginApplicationDiagram.get().asFile.readText()
            assertThat(content).startsWith("@startuml")
            assertThat(content.trim()).endsWith("@enduml")
            assertThat(content).contains("package \"dev.test\"")
            assertThat(content).contains("agent \"dev.test.plugin-a\"")
            assertThat(content).contains("agent \"dev.test.plugin-b\"")
        }

        @Test
        fun `generates dependency arrows between plugins`() {
            val srcFolder = File(tempDir, "src/main/kotlin/dev/test").apply { mkdirs() }
            File(srcFolder, "child.gradle.kts").writeText(
                """
                plugins {
                    id("dev.test.parent")
                }
                """.trimIndent()
            )
            File(srcFolder, "parent.gradle.kts").writeText("plugins { }")

            val task = createTask()
            task.pluginSrcFolders.from(File(tempDir, "src/main/kotlin"))
            task.pluginApplicationDiagram.set(File(tempDir, "diagram.puml"))

            task.analyse()

            val content = task.pluginApplicationDiagram.get().asFile.readText()
            assertThat(content).contains("\"dev.test.child\" --down--> \"dev.test.parent\"")
        }
    }

    @Nested
    inner class MultipleSourceFolders {

        @Test
        fun `handles multiple plugin source folders`() {
            val srcFolder1 = File(tempDir, "plugins-core/src/main/kotlin/dev/core").apply { mkdirs() }
            File(srcFolder1, "core.gradle.kts").writeText("plugins { }")

            val srcFolder2 = File(tempDir, "plugins-ext/src/main/kotlin/dev/ext").apply { mkdirs() }
            File(srcFolder2, "extension.gradle.kts").writeText(
                """
                plugins {
                    id("dev.core.core")
                }
                """.trimIndent()
            )

            val task = createTask()
            task.pluginSrcFolders.from(
                File(tempDir, "plugins-core/src/main/kotlin"),
                File(tempDir, "plugins-ext/src/main/kotlin")
            )
            task.pluginApplicationDiagram.set(File(tempDir, "diagram.puml"))

            task.analyse()

            val content = task.pluginApplicationDiagram.get().asFile.readText()
            assertThat(content).contains("dev.core")
            assertThat(content).contains("dev.ext")
        }
    }
}
