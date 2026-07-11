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

import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.gradle.api.GradleException
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

class DetectClasspathCollisionsTaskTest {

    @TempDir lateinit var tempDir: File

    @Test
    fun `fails when two jars provide the same class with differing bytes`() {
        val a = jar("a.jar", mapOf("com/example/Dup.class" to "one"))
        val b = jar("b.jar", mapOf("com/example/Dup.class" to "two"))

        assertThatThrownBy { runTask(a, b) }
            .isInstanceOf(GradleException::class.java)
            .hasMessageContaining("com/example/Dup.class")
    }

    @Test
    fun `passes when the duplicate class is byte-for-byte identical`() {
        val a = jar("a.jar", mapOf("com/example/Same.class" to "same"))
        val b = jar("b.jar", mapOf("com/example/Same.class" to "same"))

        runTask(a, b)
    }

    @Test
    fun `ignores module descriptors which differ per module by design`() {
        val a = jar("a.jar", mapOf("module-info.class" to "a", "META-INF/versions/9/module-info.class" to "a9"))
        val b = jar("b.jar", mapOf("module-info.class" to "b", "META-INF/versions/9/module-info.class" to "b9"))

        runTask(a, b)
    }

    @Test
    fun `ignores non-class entries`() {
        val a = jar("a.jar", mapOf("META-INF/MANIFEST.MF" to "one"))
        val b = jar("b.jar", mapOf("META-INF/MANIFEST.MF" to "two"))

        runTask(a, b)
    }

    @Test
    fun `skips class names matching the exclusions`() {
        val a = jar("a.jar", mapOf("com/example/Skipped.class" to "one"))
        val b = jar("b.jar", mapOf("com/example/Skipped.class" to "two"))

        runTask(a, b, exclusions = listOf(".*Skipped\\.class"))
    }

    private fun runTask(vararg jars: File, exclusions: List<String> = emptyList()) {
        val project = ProjectBuilder.builder().withProjectDir(tempDir).build()
        val task = project.tasks.register("detect", DetectClasspathCollisionsTask::class.java).get()
        task.classpath.setFrom(*jars)
        task.resourceExclusions.set(exclusions)
        task.detect()
    }

    private fun jar(name: String, entries: Map<String, String>): File {
        val file = File(tempDir, name)
        ZipOutputStream(file.outputStream()).use { zip ->
            entries.forEach { (entryName, content) ->
                zip.putNextEntry(ZipEntry(entryName))
                zip.write(content.toByteArray())
                zip.closeEntry()
            }
        }
        assertThat(file).exists()
        return file
    }
}
