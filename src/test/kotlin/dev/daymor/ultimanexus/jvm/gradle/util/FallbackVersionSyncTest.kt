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

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Test
import java.io.File

/**
 * Validates that fallback versions in [DependencyUtils.Fallbacks] match
 * the versions defined in `gradle/libs.versions.toml`.
 *
 * This test prevents version drift between the two sources of truth.
 */
class FallbackVersionSyncTest {

    private val tomlFile = File("gradle/libs.versions.toml")

    @Test
    fun `fallback versions match libs_versions_toml`() {
        if (!tomlFile.exists()) {
            fail<Unit>("libs.versions.toml not found at: ${tomlFile.absolutePath}")
        }

        val tomlVersions = parseVersionsFromToml(tomlFile.readText())
        val mismatches = mutableListOf<String>()

        // Check each fallback against the TOML versions
        checkVersion(
            mismatches,
            "byte-buddy-agent",
            tomlVersions,
            extractVersion(DependencyUtils.Fallbacks.BYTE_BUDDY_AGENT)
        )
        checkVersion(
            mismatches,
            "jspecify",
            tomlVersions,
            extractVersion(DependencyUtils.Fallbacks.JSPECIFY)
        )
        checkVersion(
            mismatches,
            "jsr305",
            tomlVersions,
            extractVersion(DependencyUtils.Fallbacks.JSR305)
        )
        checkVersion(
            mismatches,
            "junit-jupiter",
            tomlVersions,
            extractVersion(DependencyUtils.Fallbacks.JUNIT_JUPITER_ENGINE)
        )
        checkVersion(
            mismatches,
            "junit-jupiter",
            tomlVersions,
            DependencyUtils.Fallbacks.JUNIT_JUPITER_VERSION,
            "JUNIT_JUPITER_VERSION"
        )
        checkVersion(
            mismatches,
            "lombok",
            tomlVersions,
            extractVersion(DependencyUtils.Fallbacks.LOMBOK)
        )
        checkVersion(
            mismatches,
            "slf4j",
            tomlVersions,
            extractVersion(DependencyUtils.Fallbacks.SLF4J_SIMPLE)
        )
        checkVersion(
            mismatches,
            "spring-boot-dependencies",
            tomlVersions,
            extractVersion(DependencyUtils.Fallbacks.SPRING_BOOT_DEVTOOLS)
        )

        if (mismatches.isNotEmpty()) {
            fail<Unit>(
                buildString {
                    appendLine("Version mismatch between DependencyUtils.Fallbacks and libs.versions.toml:")
                    appendLine()
                    mismatches.forEach { appendLine(it) }
                    appendLine()
                    appendLine("Please update DependencyUtils.Fallbacks to match libs.versions.toml")
                }
            )
        }
    }

    private fun parseVersionsFromToml(content: String): Map<String, String> {
        val versions = mutableMapOf<String, String>()
        var inVersionsSection = false

        for (line in content.lines()) {
            val trimmed = line.trim()

            when {
                trimmed == "[versions]" -> inVersionsSection = true
                trimmed.startsWith("[") && trimmed != "[versions]" -> inVersionsSection = false
                inVersionsSection && trimmed.contains("=") && !trimmed.startsWith("#") -> {
                    val parts = trimmed.split("=", limit = 2)
                    if (parts.size == 2) {
                        val key = parts[0].trim()
                        val value = parts[1].trim().removeSurrounding("\"")
                        versions[key] = value
                    }
                }
            }
        }

        return versions
    }

    private fun extractVersion(dependency: String): String {
        // Format: "group:artifact:version"
        return dependency.substringAfterLast(":")
    }

    private fun checkVersion(
        mismatches: MutableList<String>,
        tomlKey: String,
        tomlVersions: Map<String, String>,
        fallbackVersion: String,
        fallbackName: String = tomlKey.uppercase().replace("-", "_")
    ) {
        val tomlVersion = tomlVersions[tomlKey]
        if (tomlVersion == null) {
            mismatches.add("  - $tomlKey: not found in libs.versions.toml")
        } else if (tomlVersion != fallbackVersion) {
            mismatches.add(
                "  - $fallbackName: expected '$tomlVersion' (from TOML) but found '$fallbackVersion' (in Fallbacks)"
            )
        }
    }

    @Test
    fun `toml file exists and is readable`() {
        assertEquals(true, tomlFile.exists(), "libs.versions.toml should exist")
        assertEquals(true, tomlFile.canRead(), "libs.versions.toml should be readable")
    }
}
