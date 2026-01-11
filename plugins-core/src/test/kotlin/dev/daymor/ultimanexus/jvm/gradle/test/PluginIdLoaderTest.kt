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

package dev.daymor.ultimanexus.jvm.gradle.test

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class PluginIdLoaderTest {

    @TempDir
    lateinit var tempDir: File

    @Nested
    inner class LoadPluginIds {

        @Test
        fun `finds gradle kts files in directory`() {
            File(tempDir, "plugin-a.gradle.kts").createNewFile()
            File(tempDir, "plugin-b.gradle.kts").createNewFile()

            val ids = PluginIdLoader.loadPluginIds(tempDir)

            assertThat(ids).containsExactlyInAnyOrder("plugin-a", "plugin-b")
        }

        @Test
        fun `finds gradle kts files recursively`() {
            File(tempDir, "base").mkdir()
            File(tempDir, "base/core.gradle.kts").createNewFile()
            File(tempDir, "feature").mkdir()
            File(tempDir, "feature/web.gradle.kts").createNewFile()

            val ids = PluginIdLoader.loadPluginIds(tempDir)

            assertThat(ids).containsExactlyInAnyOrder("core", "web")
        }

        @Test
        fun `ignores non gradle kts files`() {
            File(tempDir, "plugin.gradle.kts").createNewFile()
            File(tempDir, "build.gradle").createNewFile()
            File(tempDir, "Utils.kt").createNewFile()

            val ids = PluginIdLoader.loadPluginIds(tempDir)

            assertThat(ids).containsExactly("plugin")
        }

        @Test
        fun `returns empty list for empty directory`() {
            val ids = PluginIdLoader.loadPluginIds(tempDir)

            assertThat(ids).isEmpty()
        }

        @Test
        fun `returns sorted plugin ids`() {
            File(tempDir, "zebra.gradle.kts").createNewFile()
            File(tempDir, "alpha.gradle.kts").createNewFile()
            File(tempDir, "middle.gradle.kts").createNewFile()

            val ids = PluginIdLoader.loadPluginIds(tempDir)

            assertThat(ids).isSorted
            assertThat(ids).containsExactlyInAnyOrder("alpha", "middle", "zebra")
        }

        @Test
        fun `handles deeply nested directories`() {
            File(tempDir, "a/b/c/d").mkdirs()
            File(tempDir, "a/b/c/d/deep.gradle.kts").createNewFile()

            val ids = PluginIdLoader.loadPluginIds(tempDir)

            assertThat(ids).containsExactly("deep")
        }
    }

    @Nested
    inner class LoadPluginIdsWithPrefix {

        @Test
        fun `adds prefix to all plugin ids`() {
            File(tempDir, "checkstyle.gradle.kts").createNewFile()
            File(tempDir, "pmd.gradle.kts").createNewFile()

            val ids = PluginIdLoader.loadPluginIdsWithPrefix(tempDir, "dev.example.check")

            assertThat(ids).containsExactlyInAnyOrder(
                "dev.example.check.checkstyle",
                "dev.example.check.pmd"
            )
        }

        @Test
        fun `returns plain ids when prefix is empty`() {
            File(tempDir, "plugin.gradle.kts").createNewFile()

            val ids = PluginIdLoader.loadPluginIdsWithPrefix(tempDir, "")

            assertThat(ids).containsExactlyInAnyOrder("plugin")
        }

        @Test
        fun `handles empty directory with prefix`() {
            val ids = PluginIdLoader.loadPluginIdsWithPrefix(tempDir, "dev.example")

            assertThat(ids).isEmpty()
        }

        @Test
        fun `preserves sorting with prefix`() {
            File(tempDir, "zebra.gradle.kts").createNewFile()
            File(tempDir, "alpha.gradle.kts").createNewFile()

            val ids = PluginIdLoader.loadPluginIdsWithPrefix(tempDir, "prefix")

            assertThat(ids).isSorted
            assertThat(ids).containsExactlyInAnyOrder("prefix.alpha", "prefix.zebra")
        }
    }
}
