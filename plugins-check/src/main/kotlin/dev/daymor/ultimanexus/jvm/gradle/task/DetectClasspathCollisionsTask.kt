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
import java.security.MessageDigest
import java.util.zip.ZipFile
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

/**
 * Fails the build when two or more jars on the classpath contribute the
 * same entry, the classic "classpath hell" duplicate that makes which
 * class wins depend on jar ordering.
 *
 * The classpath is captured as a [Classpath] input at configuration
 * time and the scan reads only the jar bytes at execution time — no
 * live Gradle model is touched — so the task is compatible with the
 * configuration cache, unlike the unmaintained collision plugins. It
 * parses no class or Kotlin metadata; it groups {@code .class} entry
 * names across jars, suppresses duplicates whose bytes are
 * byte-for-byte identical, and skips class names matching the
 * configured exclusion patterns.
 */
@DisableCachingByDefault(because = "Cheap classpath scan; output is pass/fail only")
abstract class DetectClasspathCollisionsTask : DefaultTask() {

    @get:Classpath abstract val classpath: ConfigurableFileCollection

    @get:Input abstract val resourceExclusions: ListProperty<String>

    @TaskAction
    fun detect() {
        val excludes = resourceExclusions.get().map(String::toRegex)
        val jars = classpath.files.filter { it.isFile && it.name.endsWith(".jar") }
        val owners = LinkedHashMap<String, MutableList<JarEntryDigest>>()

        for (jar in jars) {
            collectEntries(jar, excludes, owners)
        }

        val collisions =
            owners.filterValues { digests -> digests.size > 1 && digests.mapTo(HashSet()) { it.sha256 }.size > 1 }

        if (collisions.isNotEmpty()) {

            val report =
                collisions.entries.joinToString("\n") { (entry, digests) ->
                    "  $entry${digests.joinToString("") { "\n    - ${it.jar}" }}"
                }
            throw GradleException(
                "Classpath collisions detected — the same entry is provided with differing bytes by multiple jars:\n" +
                    report
            )
        }
        logger.lifecycle("No classpath collisions across ${jars.size} jar(s).")
    }

    private fun collectEntries(
        jar: File,
        excludes: List<Regex>,
        owners: MutableMap<String, MutableList<JarEntryDigest>>,
    ) {
        ZipFile(jar).use { zip ->
            for (entry in zip.entries()) {
                if (entry.isDirectory || !isChecked(entry.name, excludes)) {
                    continue
                }
                val sha = zip.getInputStream(entry).use { sha256(it.readBytes()) }
                owners.getOrPut(entry.name) { mutableListOf() }.add(JarEntryDigest(jar.name, sha))
            }
        }
    }

    private fun isChecked(name: String, excludes: List<Regex>): Boolean {
        return name.endsWith(".class") && !isModuleDescriptor(name) && excludes.none { it.matches(name) }
    }

    private fun isModuleDescriptor(name: String): Boolean {
        return name == "module-info.class" ||
            (name.startsWith("META-INF/versions/") && name.endsWith("/module-info.class"))
    }

    private fun sha256(bytes: ByteArray): String =
        MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }

    private data class JarEntryDigest(val jar: String, val sha256: String)
}
