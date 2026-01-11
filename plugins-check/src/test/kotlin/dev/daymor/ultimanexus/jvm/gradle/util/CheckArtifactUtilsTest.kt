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

import dev.daymor.ultimanexus.jvm.gradle.config.Messages
import dev.daymor.ultimanexus.jvm.gradle.config.UltimaNexusConfig
import dev.daymor.ultimanexus.jvm.gradle.util.CheckArtifactUtils.createCheckConfiguration
import dev.daymor.ultimanexus.jvm.gradle.util.CheckArtifactUtils.resolveCheckJar
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.gradle.api.Action
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.util.Optional
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class CheckArtifactUtilsTest {

    @TempDir
    lateinit var tempDir: File

    private fun createTestJar(vararg entries: Pair<String, String>): File {
        val jarFile = File(tempDir, "test.jar")
        ZipOutputStream(jarFile.outputStream()).use { zip ->
            entries.forEach { (name, content) ->
                zip.putNextEntry(ZipEntry(name))
                zip.write(content.toByteArray())
                zip.closeEntry()
            }
        }
        return jarFile
    }

    @Nested
    inner class GetCheckArtifactName {

        @Test
        fun `returns artifact name when configured`() {
            val project = mockk<Project>()
            val extensions = mockk<ExtensionContainer>()
            val config = mockk<UltimaNexusConfig>()
            val checkArtifactNameProperty = mockk<Property<String>>()
            every { project.extensions } returns extensions
            every { extensions.findByType(UltimaNexusConfig::class.java) } returns config
            every { config.checkArtifactName } returns checkArtifactNameProperty
            every { checkArtifactNameProperty.orNull } returns "my-check-artifact"

            val result = CheckArtifactUtils.getCheckArtifactName(project)

            assertThat(result).isEqualTo("my-check-artifact")
        }

        @Test
        fun `throws when artifact name not configured`() {
            val project = mockk<Project>()
            val extensions = mockk<ExtensionContainer>()
            val config = mockk<UltimaNexusConfig>()
            val checkArtifactNameProperty = mockk<Property<String>>()
            every { project.extensions } returns extensions
            every { extensions.findByType(UltimaNexusConfig::class.java) } returns config
            every { config.checkArtifactName } returns checkArtifactNameProperty
            every { checkArtifactNameProperty.orNull } returns null

            assertThatThrownBy {
                CheckArtifactUtils.getCheckArtifactName(project)
            }.isInstanceOf(GradleException::class.java)
                .hasMessage(Messages.CHECK_ARTIFACT_NAME_REQUIRED)
        }
    }

    @Nested
    inner class CreateCheckConfiguration {

        @Test
        @Suppress("UNCHECKED_CAST")
        fun `creates configuration with correct properties`() {
            val project = mockk<Project>()
            val extensions = mockk<ExtensionContainer>()
            val config = mockk<UltimaNexusConfig>()
            val checkArtifactNameProperty = mockk<Property<String>>()
            val configurations = mockk<ConfigurationContainer>()
            val configuration = mockk<Configuration>(relaxed = true)
            val dependencies = mockk<DependencyHandler>()
            val versionCatalog = mockk<VersionCatalog>()
            val libraryProvider = mockk<Provider<MinimalExternalModuleDependency>>()

            every { project.extensions } returns extensions
            every { extensions.findByType(UltimaNexusConfig::class.java) } returns config
            every { config.checkArtifactName } returns checkArtifactNameProperty
            every { checkArtifactNameProperty.orNull } returns "check-artifact"
            every { project.configurations } returns configurations
            every { configurations.create(eq("testConfig"), any<Action<Configuration>>()) } answers {
                val action = secondArg<Action<Configuration>>()
                action.execute(configuration)
                configuration
            }
            every { project.dependencies } returns dependencies
            every { versionCatalog.findLibrary("check-artifact") } returns Optional.of(libraryProvider)
            every { dependencies.add("testConfig", libraryProvider) } returns null

            val result = project.createCheckConfiguration("testConfig", versionCatalog)

            assertThat(result).isEqualTo(configuration)
            verify { configuration.isCanBeConsumed = false }
            verify { configuration.isCanBeResolved = true }
            verify { dependencies.add("testConfig", libraryProvider) }
        }
    }

    @Nested
    inner class ResolveCheckJar {

        @Test
        fun `returns jar file when found`() {
            val project = mockk<Project>()
            val extensions = mockk<ExtensionContainer>()
            val config = mockk<UltimaNexusConfig>()
            val checkArtifactNameProperty = mockk<Property<String>>()
            val configuration = mockk<Configuration>()
            val jarFile = File(tempDir, "my-artifact-1.0.0.jar")
            jarFile.createNewFile()

            every { project.extensions } returns extensions
            every { extensions.findByType(UltimaNexusConfig::class.java) } returns config
            every { config.checkArtifactName } returns checkArtifactNameProperty
            every { checkArtifactNameProperty.orNull } returns "my-artifact"
            every { configuration.resolve() } returns setOf(jarFile)

            val result = configuration.resolveCheckJar(project)

            assertThat(result).isEqualTo(jarFile)
        }

        @Test
        fun `throws when jar not found in resolved files`() {
            val project = mockk<Project>()
            val extensions = mockk<ExtensionContainer>()
            val config = mockk<UltimaNexusConfig>()
            val checkArtifactNameProperty = mockk<Property<String>>()
            val configuration = mockk<Configuration>()
            val otherFile = File(tempDir, "other-artifact-1.0.0.jar")
            otherFile.createNewFile()

            every { project.extensions } returns extensions
            every { extensions.findByType(UltimaNexusConfig::class.java) } returns config
            every { config.checkArtifactName } returns checkArtifactNameProperty
            every { checkArtifactNameProperty.orNull } returns "my-artifact"
            every { configuration.resolve() } returns setOf(otherFile)

            assertThatThrownBy {
                configuration.resolveCheckJar(project)
            }.isInstanceOf(GradleException::class.java)
                .hasMessage(Messages.checkArtifactNotResolved("my-artifact"))
        }

        @Test
        fun `throws when no files resolved`() {
            val project = mockk<Project>()
            val extensions = mockk<ExtensionContainer>()
            val config = mockk<UltimaNexusConfig>()
            val checkArtifactNameProperty = mockk<Property<String>>()
            val configuration = mockk<Configuration>()

            every { project.extensions } returns extensions
            every { extensions.findByType(UltimaNexusConfig::class.java) } returns config
            every { config.checkArtifactName } returns checkArtifactNameProperty
            every { checkArtifactNameProperty.orNull } returns "my-artifact"
            every { configuration.resolve() } returns emptySet()

            assertThatThrownBy {
                configuration.resolveCheckJar(project)
            }.isInstanceOf(GradleException::class.java)
                .hasMessage(Messages.checkArtifactNotResolved("my-artifact"))
        }
    }

    @Nested
    inner class ReadFromJar {

        @Test
        fun `extracts entry content from jar`() {
            val jarFile = createTestJar("config/checkstyle.xml" to "<config>test</config>")

            val content = CheckArtifactUtils.readFromJar(jarFile, "config/checkstyle.xml")

            assertThat(content).isEqualTo("<config>test</config>")
        }

        @Test
        fun `throws error when entry not found`() {
            val jarFile = createTestJar("other.txt" to "content")

            assertThatThrownBy {
                CheckArtifactUtils.readFromJar(jarFile, "missing.xml")
            }.isInstanceOf(IllegalStateException::class.java)
                .hasMessageContaining("Entry missing.xml not found")
        }

        @Test
        fun `handles nested paths in jar`() {
            val jarFile = createTestJar(
                "deep/nested/path/file.txt" to "nested content"
            )

            val content = CheckArtifactUtils.readFromJar(jarFile, "deep/nested/path/file.txt")

            assertThat(content).isEqualTo("nested content")
        }

        @Test
        fun `reads multiple entries from same jar`() {
            val jarFile = createTestJar(
                "file1.txt" to "content1",
                "file2.txt" to "content2"
            )

            val content1 = CheckArtifactUtils.readFromJar(jarFile, "file1.txt")
            val content2 = CheckArtifactUtils.readFromJar(jarFile, "file2.txt")

            assertThat(content1).isEqualTo("content1")
            assertThat(content2).isEqualTo("content2")
        }

        @Test
        fun `handles empty file content`() {
            val jarFile = createTestJar("empty.txt" to "")

            val content = CheckArtifactUtils.readFromJar(jarFile, "empty.txt")

            assertThat(content).isEmpty()
        }

        @Test
        fun `handles multiline content`() {
            val multilineContent = """
                line1
                line2
                line3
            """.trimIndent()
            val jarFile = createTestJar("multiline.txt" to multilineContent)

            val content = CheckArtifactUtils.readFromJar(jarFile, "multiline.txt")

            assertThat(content).isEqualTo(multilineContent)
        }

        @Test
        fun `handles special characters in content`() {
            val specialContent = "<xml attr=\"value\">content & more</xml>"
            val jarFile = createTestJar("special.xml" to specialContent)

            val content = CheckArtifactUtils.readFromJar(jarFile, "special.xml")

            assertThat(content).isEqualTo(specialContent)
        }
    }
}
