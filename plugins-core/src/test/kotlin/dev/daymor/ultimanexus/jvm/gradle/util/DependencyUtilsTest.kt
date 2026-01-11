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
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.artifacts.VersionConstraint
import org.gradle.api.provider.Provider
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.Optional

class DependencyUtilsTest {

    @Nested
    inner class GetLibsCatalogOrNull {

        @Test
        fun `returns catalog when present`() {
            val project = mockk<Project>()
            val catalogsExtension = mockk<VersionCatalogsExtension>()
            val versionCatalog = mockk<VersionCatalog>()
            every { project.extensions.findByType(VersionCatalogsExtension::class.java) } returns catalogsExtension
            every { catalogsExtension.find("libs") } returns Optional.of(versionCatalog)

            val result = DependencyUtils.getLibsCatalogOrNull(project)

            assertThat(result).isEqualTo(versionCatalog)
        }

        @Test
        fun `returns null when catalogs extension not found`() {
            val project = mockk<Project>()
            every { project.extensions.findByType(VersionCatalogsExtension::class.java) } returns null

            val result = DependencyUtils.getLibsCatalogOrNull(project)

            assertThat(result).isNull()
        }

        @Test
        fun `returns null when libs catalog not found`() {
            val project = mockk<Project>()
            val catalogsExtension = mockk<VersionCatalogsExtension>()
            every { project.extensions.findByType(VersionCatalogsExtension::class.java) } returns catalogsExtension
            every { catalogsExtension.find("libs") } returns Optional.empty()

            val result = DependencyUtils.getLibsCatalogOrNull(project)

            assertThat(result).isNull()
        }

        @Test
        fun `returns null when exception is thrown`() {
            val project = mockk<Project>()
            every { project.extensions.findByType(VersionCatalogsExtension::class.java) } throws RuntimeException("test")

            val result = DependencyUtils.getLibsCatalogOrNull(project)

            assertThat(result).isNull()
        }
    }

    @Nested
    inner class GetLibsCatalog {

        @Test
        fun `returns catalog when present`() {
            val project = mockk<Project>()
            val catalogsExtension = mockk<VersionCatalogsExtension>()
            val versionCatalog = mockk<VersionCatalog>()
            every { project.extensions.findByType(VersionCatalogsExtension::class.java) } returns catalogsExtension
            every { catalogsExtension.find("libs") } returns Optional.of(versionCatalog)

            val result = DependencyUtils.getLibsCatalog(project)

            assertThat(result).isEqualTo(versionCatalog)
        }

        @Test
        fun `throws when catalog not found`() {
            val project = mockk<Project>()
            every { project.extensions.findByType(VersionCatalogsExtension::class.java) } returns null

            assertThatThrownBy {
                DependencyUtils.getLibsCatalog(project)
            }.isInstanceOf(GradleException::class.java)
                .hasMessage(Messages.VERSION_CATALOG_NOT_FOUND)
        }
    }

    @Nested
    inner class GetLibrary {

        @Test
        fun `returns library provider when found`() {
            val versionCatalog = mockk<VersionCatalog>()
            val libraryProvider = mockk<Provider<MinimalExternalModuleDependency>>()
            every { versionCatalog.findLibrary("assertj-core") } returns Optional.of(libraryProvider)

            val result = DependencyUtils.getLibrary(versionCatalog, "assertj-core")

            assertThat(result).isEqualTo(libraryProvider)
        }

        @Test
        fun `throws when library not found`() {
            val versionCatalog = mockk<VersionCatalog>()
            every { versionCatalog.findLibrary("missing-lib") } returns Optional.empty()

            assertThatThrownBy {
                DependencyUtils.getLibrary(versionCatalog, "missing-lib")
            }.isInstanceOf(GradleException::class.java)
                .hasMessage(Messages.libraryNotFound("missing-lib"))
        }
    }

    @Nested
    inner class GetLibraryOrNull {

        @Test
        fun `returns library provider when found`() {
            val versionCatalog = mockk<VersionCatalog>()
            val libraryProvider = mockk<Provider<MinimalExternalModuleDependency>>()
            every { versionCatalog.findLibrary("assertj-core") } returns Optional.of(libraryProvider)

            val result = DependencyUtils.getLibraryOrNull(versionCatalog, "assertj-core")

            assertThat(result).isEqualTo(libraryProvider)
        }

        @Test
        fun `returns null when library not found`() {
            val versionCatalog = mockk<VersionCatalog>()
            every { versionCatalog.findLibrary("missing-lib") } returns Optional.empty()

            val result = DependencyUtils.getLibraryOrNull(versionCatalog, "missing-lib")

            assertThat(result).isNull()
        }
    }

    @Nested
    inner class GetVersion {

        @Test
        fun `returns version string when found`() {
            val versionCatalog = mockk<VersionCatalog>()
            val versionConstraint = mockk<VersionConstraint>()
            every { versionCatalog.findVersion("jspecify") } returns Optional.of(versionConstraint)
            every { versionConstraint.toString() } returns "1.0.0"

            val result = DependencyUtils.getVersion(versionCatalog, "jspecify")

            assertThat(result).isEqualTo("1.0.0")
        }

        @Test
        fun `throws when version not found`() {
            val versionCatalog = mockk<VersionCatalog>()
            every { versionCatalog.findVersion("missing-version") } returns Optional.empty()

            assertThatThrownBy {
                DependencyUtils.getVersion(versionCatalog, "missing-version")
            }.isInstanceOf(GradleException::class.java)
                .hasMessage(Messages.versionNotFound("missing-version"))
        }
    }

    @Nested
    inner class GetVersionOrNull {

        @Test
        fun `returns version string when found`() {
            val versionCatalog = mockk<VersionCatalog>()
            val versionConstraint = mockk<VersionConstraint>()
            every { versionCatalog.findVersion("jspecify") } returns Optional.of(versionConstraint)
            every { versionConstraint.toString() } returns "1.0.0"

            val result = DependencyUtils.getVersionOrNull(versionCatalog, "jspecify")

            assertThat(result).isEqualTo("1.0.0")
        }

        @Test
        fun `returns null when version not found`() {
            val versionCatalog = mockk<VersionCatalog>()
            every { versionCatalog.findVersion("missing-version") } returns Optional.empty()

            val result = DependencyUtils.getVersionOrNull(versionCatalog, "missing-version")

            assertThat(result).isNull()
        }
    }
}
