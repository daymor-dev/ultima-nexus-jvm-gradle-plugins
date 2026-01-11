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

import java.io.File
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class FallbackVersionSyncTest {

    private val tomlFile = File("../gradle/libs.versions.toml")

    @Test
    fun `toml file exists and is readable`() {
        assertTrue(tomlFile.exists()) { "Version catalog file not found at ${tomlFile.absolutePath}" }
        assertTrue(tomlFile.canRead()) { "Version catalog file is not readable" }
    }

    @Test
    fun `fallback versions match libs_versions_toml`() {
        val tomlContent = tomlFile.readText()
        val versions = parseVersionsFromToml(tomlContent)

        assertVersionMatches("byte-buddy-agent", versions, DependencyUtils.FallbackVersions.BYTE_BUDDY_AGENT)
        assertVersionMatches("checkstyle", versions, DependencyUtils.FallbackVersions.CHECKSTYLE)
        assertVersionMatches("jspecify", versions, DependencyUtils.FallbackVersions.JSPECIFY)
        assertVersionMatches("jsr305", versions, DependencyUtils.FallbackVersions.JSR305)
        assertVersionMatches("junit-jupiter", versions, DependencyUtils.FallbackVersions.JUNIT_JUPITER)
        assertVersionMatches("lombok", versions, DependencyUtils.FallbackVersions.LOMBOK)
        assertVersionMatches("pmd", versions, DependencyUtils.FallbackVersions.PMD)
        assertVersionMatches("slf4j", versions, DependencyUtils.FallbackVersions.SLF4J)
        assertVersionMatches("spring-boot-dependencies", versions, DependencyUtils.FallbackVersions.SPRING_BOOT)
    }

    private fun parseVersionsFromToml(content: String): Map<String, String> {
        val versions = mutableMapOf<String, String>()
        var inVersionsSection = false

        content.lines().forEach { line ->
            val trimmed = line.trim()
            when {
                trimmed == "[versions]" -> inVersionsSection = true
                trimmed.startsWith("[") -> inVersionsSection = false
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

    private fun assertVersionMatches(
        tomlKey: String,
        versions: Map<String, String>,
        fallbackVersion: String
    ) {
        val expectedVersion = versions[tomlKey]
            ?: error("Version key '$tomlKey' not found in libs.versions.toml")

        assertEquals(
            expectedVersion,
            fallbackVersion,
            "FallbackVersions.$tomlKey does not match libs.versions.toml. " +
                "Expected: $expectedVersion, Found: $fallbackVersion"
        )
    }
}
