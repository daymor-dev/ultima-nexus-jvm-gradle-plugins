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

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class ProjectUtilsTest {

    @TempDir
    lateinit var tempDir: File

    @Nested
    inner class IsExcludedPatternMatching {

        @Test
        fun `exact directory name match excludes directory`() {
            val result = ProjectUtils.isExcluded("build", "build", listOf("build"))

            assertThat(result).isTrue()
        }

        @Test
        fun `directory name not in patterns is not excluded`() {
            val result = ProjectUtils.isExcluded("src", "src", listOf("build", "out"))

            assertThat(result).isFalse()
        }

        @Test
        fun `wildcard prefix pattern matches suffix`() {
            val result = ProjectUtils.isExcluded("my-gradle-plugins", "my-gradle-plugins", listOf("*-gradle-plugins"))

            assertThat(result).isTrue()
        }

        @Test
        fun `wildcard prefix pattern does not match different suffix`() {
            val result = ProjectUtils.isExcluded("my-plugins", "my-plugins", listOf("*-gradle-plugins"))

            assertThat(result).isFalse()
        }

        @Test
        fun `path pattern with slash matches relative path`() {
            val result = ProjectUtils.isExcluded("gradle${File.separator}plugins", "plugins", listOf("gradle/plugins"))

            assertThat(result).isTrue()
        }

        @Test
        fun `path pattern with slash does not match different path`() {
            val result = ProjectUtils.isExcluded("src${File.separator}plugins", "plugins", listOf("gradle/plugins"))

            assertThat(result).isFalse()
        }

        @Test
        fun `empty patterns list excludes nothing`() {
            val result = ProjectUtils.isExcluded("anything", "anything", emptyList())

            assertThat(result).isFalse()
        }

        @Test
        fun `multiple patterns check all`() {
            assertThat(ProjectUtils.isExcluded("build", "build", listOf("out", "build", "dist"))).isTrue()
            assertThat(ProjectUtils.isExcluded("out", "out", listOf("out", "build", "dist"))).isTrue()
            assertThat(ProjectUtils.isExcluded("dist", "dist", listOf("out", "build", "dist"))).isTrue()
            assertThat(ProjectUtils.isExcluded("src", "src", listOf("out", "build", "dist"))).isFalse()
        }
    }

    @Nested
    inner class DirectoryStructureTests {

        @Test
        fun `includes directories with build gradle kts at depth 1`() {
            File(tempDir, "module-a").mkdir()
            File(tempDir, "module-a/build.gradle.kts").createNewFile()
            File(tempDir, "module-b").mkdir()
            File(tempDir, "module-b/build.gradle.kts").createNewFile()
            File(tempDir, "no-build").mkdir()

            val dirsWithBuildFile = tempDir.walk()
                .maxDepth(1)
                .filter { dir ->
                    File(dir, "build.gradle.kts").exists() && tempDir != dir
                }
                .map { it.name }
                .toList()

            assertThat(dirsWithBuildFile).containsExactlyInAnyOrder("module-a", "module-b")
        }

        @Test
        fun `respects max depth parameter`() {
            File(tempDir, "level1").mkdir()
            File(tempDir, "level1/build.gradle.kts").createNewFile()
            File(tempDir, "level1/level2").mkdirs()
            File(tempDir, "level1/level2/build.gradle.kts").createNewFile()
            File(tempDir, "level1/level2/level3").mkdirs()
            File(tempDir, "level1/level2/level3/build.gradle.kts").createNewFile()

            val depth1Results = tempDir.walk()
                .maxDepth(1)
                .filter { File(it, "build.gradle.kts").exists() && tempDir != it }
                .map { it.toRelativeString(tempDir) }
                .toList()

            val depth2Results = tempDir.walk()
                .maxDepth(2)
                .filter { File(it, "build.gradle.kts").exists() && tempDir != it }
                .map { it.toRelativeString(tempDir) }
                .toList()

            assertThat(depth1Results).hasSize(1)
            assertThat(depth2Results).hasSize(2)
        }

        @Test
        fun `excludes root directory from results`() {
            File(tempDir, "build.gradle.kts").createNewFile()
            File(tempDir, "submodule").mkdir()
            File(tempDir, "submodule/build.gradle.kts").createNewFile()

            val results = tempDir.walk()
                .maxDepth(1)
                .filter { File(it, "build.gradle.kts").exists() && tempDir != it }
                .map { it.name }
                .toList()

            assertThat(results).containsExactly("submodule")
            assertThat(results).doesNotContain(tempDir.name)
        }

        @Test
        fun `handles nested directory structures`() {
            File(tempDir, "parent/child").mkdirs()
            File(tempDir, "parent/child/build.gradle.kts").createNewFile()

            val results = tempDir.walk()
                .maxDepth(2)
                .filter { File(it, "build.gradle.kts").exists() && tempDir != it }
                .map { it.toRelativeString(tempDir) }
                .toList()

            assertThat(results).containsExactly("parent${File.separator}child")
        }

        @Test
        fun `separates folder and module from nested path`() {
            val relativePath = "parent${File.separatorChar}child"

            val containsSeparator = relativePath.contains(File.separatorChar)
            val folder = relativePath.substringBeforeLast(File.separatorChar)
            val module = relativePath.substringAfterLast(File.separatorChar)

            assertThat(containsSeparator).isTrue()
            assertThat(folder).isEqualTo("parent")
            assertThat(module).isEqualTo("child")
        }

        @Test
        fun `handles single level path without separator`() {
            val relativePath = "module"

            val containsSeparator = relativePath.contains(File.separatorChar)

            assertThat(containsSeparator).isFalse()
        }
    }
}
