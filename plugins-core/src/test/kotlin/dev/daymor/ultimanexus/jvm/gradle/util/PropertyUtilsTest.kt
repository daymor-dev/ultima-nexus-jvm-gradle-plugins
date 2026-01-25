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

import dev.daymor.ultimanexus.jvm.gradle.util.PropertyUtils.conventionFromProperty
import dev.daymor.ultimanexus.jvm.gradle.util.PropertyUtils.conventionIfNotNull
import dev.daymor.ultimanexus.jvm.gradle.util.PropertyUtils.findPropertyAsBoolean
import dev.daymor.ultimanexus.jvm.gradle.util.PropertyUtils.findPropertyAsInt
import dev.daymor.ultimanexus.jvm.gradle.util.PropertyUtils.findPropertyOrNull
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class PropertyUtilsTest {

    @Nested
    inner class FindPropertyOrNull {

        @Test
        fun `returns null when property not set`() {
            val project = mockk<Project>()
            every { project.findProperty("test.key") } returns null

            val result = project.findPropertyOrNull("test.key")

            assertThat(result).isNull()
        }

        @Test
        fun `returns value when property is set`() {
            val project = mockk<Project>()
            every { project.findProperty("test.key") } returns "test-value"

            val result = project.findPropertyOrNull("test.key")

            assertThat(result).isEqualTo("test-value")
        }

        @Test
        fun `converts non-string values to string`() {
            val project = mockk<Project>()
            every { project.findProperty("test.key") } returns 42

            val result = project.findPropertyOrNull("test.key")

            assertThat(result).isEqualTo("42")
        }
    }

    @Nested
    inner class FindPropertyAsBoolean {

        @Test
        fun `returns true when property is true`() {
            val project = mockk<Project>()
            every { project.findProperty("test.enabled") } returns "true"

            val result = project.findPropertyAsBoolean("test.enabled", false)

            assertThat(result).isTrue()
        }

        @Test
        fun `returns false when property is false`() {
            val project = mockk<Project>()
            every { project.findProperty("test.enabled") } returns "false"

            val result = project.findPropertyAsBoolean("test.enabled", true)

            assertThat(result).isFalse()
        }

        @Test
        fun `returns default when property not set`() {
            val project = mockk<Project>()
            every { project.findProperty("test.enabled") } returns null

            val result = project.findPropertyAsBoolean("test.enabled", true)

            assertThat(result).isTrue()
        }

        @Test
        fun `returns false for invalid boolean value`() {
            val project = mockk<Project>()
            every { project.findProperty("test.enabled") } returns "invalid"

            val result = project.findPropertyAsBoolean("test.enabled", true)

            assertThat(result).isFalse()
        }
    }

    @Nested
    inner class FindPropertyAsInt {

        @Test
        fun `returns int value when property is valid integer`() {
            val project = mockk<Project>()
            every { project.findProperty("test.count") } returns "42"

            val result = project.findPropertyAsInt("test.count", 0)

            assertThat(result).isEqualTo(42)
        }

        @Test
        fun `returns default when property not set`() {
            val project = mockk<Project>()
            every { project.findProperty("test.count") } returns null

            val result = project.findPropertyAsInt("test.count", 10)

            assertThat(result).isEqualTo(10)
        }

        @Test
        fun `returns default for invalid integer value`() {
            val project = mockk<Project>()
            every { project.findProperty("test.count") } returns "not-a-number"

            val result = project.findPropertyAsInt("test.count", 5)

            assertThat(result).isEqualTo(5)
        }

        @Test
        fun `returns default for empty value`() {
            val project = mockk<Project>()
            every { project.findProperty("test.count") } returns ""

            val result = project.findPropertyAsInt("test.count", 7)

            assertThat(result).isEqualTo(7)
        }
    }

    @Nested
    inner class ConventionIfNotNull {

        @Test
        fun `sets convention when value provided`() {
            val property = mockk<Property<String>>(relaxed = true)

            property.conventionIfNotNull("test-value")

            verify { property.convention("test-value") }
        }

        @Test
        fun `skips convention when value is null`() {
            val property = mockk<Property<String>>(relaxed = true)

            property.conventionIfNotNull(null)

            verify(exactly = 0) { property.convention(any<String>()) }
        }
    }

    @Nested
    inner class StringConventionFromProperty {

        @Test
        fun `sets convention when property exists`() {
            val property = mockk<Property<String>>(relaxed = true)
            val project = mockk<Project>()
            every { project.findProperty("test.key") } returns "value-from-property"

            property.conventionFromProperty(project, "test.key")

            verify { property.convention("value-from-property") }
        }

        @Test
        fun `skips convention when property not set`() {
            val property = mockk<Property<String>>(relaxed = true)
            val project = mockk<Project>()
            every { project.findProperty("test.key") } returns null

            property.conventionFromProperty(project, "test.key")

            verify(exactly = 0) { property.convention(any<String>()) }
        }
    }

    @Nested
    inner class BooleanConventionFromProperty {

        @Test
        fun `sets convention to true from property`() {
            val property = mockk<Property<Boolean>>(relaxed = true)
            val project = mockk<Project>()
            every { project.findProperty("test.enabled") } returns "true"

            property.conventionFromProperty(project, "test.enabled", false)

            verify { property.convention(true) }
        }

        @Test
        fun `sets convention to default when property not set`() {
            val property = mockk<Property<Boolean>>(relaxed = true)
            val project = mockk<Project>()
            every { project.findProperty("test.enabled") } returns null

            property.conventionFromProperty(project, "test.enabled", true)

            verify { property.convention(true) }
        }
    }

    @Nested
    inner class IntConventionFromProperty {

        @Test
        fun `sets convention from property value`() {
            val property = mockk<Property<Int>>(relaxed = true)
            val project = mockk<Project>()
            every { project.findProperty("test.count") } returns "99"

            property.conventionFromProperty(project, "test.count", 0)

            verify { property.convention(99) }
        }

        @Test
        fun `sets convention to default when property not set`() {
            val property = mockk<Property<Int>>(relaxed = true)
            val project = mockk<Project>()
            every { project.findProperty("test.count") } returns null

            property.conventionFromProperty(project, "test.count", 25)

            verify { property.convention(25) }
        }
    }
}
