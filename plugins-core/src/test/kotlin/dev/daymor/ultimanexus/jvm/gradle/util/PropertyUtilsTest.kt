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

import dev.daymor.ultimanexus.jvm.gradle.util.PropertyUtils.conventionFromGradleProperty
import dev.daymor.ultimanexus.jvm.gradle.util.PropertyUtils.conventionIfNotNull
import dev.daymor.ultimanexus.jvm.gradle.util.PropertyUtils.gradlePropertyAsBoolean
import dev.daymor.ultimanexus.jvm.gradle.util.PropertyUtils.gradlePropertyAsInt
import dev.daymor.ultimanexus.jvm.gradle.util.PropertyUtils.gradlePropertyOrNull
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class PropertyUtilsTest {

    @Nested
    inner class GradlePropertyOrNull {

        @Test
        fun `returns null when property not set`() {
            val providers = mockk<ProviderFactory>()
            val provider = mockk<Provider<String>>()
            every { providers.gradleProperty("test.key") } returns provider
            every { provider.orNull } returns null

            val result = providers.gradlePropertyOrNull("test.key")

            assertThat(result).isNull()
        }

        @Test
        fun `returns value when property is set`() {
            val providers = mockk<ProviderFactory>()
            val provider = mockk<Provider<String>>()
            every { providers.gradleProperty("test.key") } returns provider
            every { provider.orNull } returns "test-value"

            val result = providers.gradlePropertyOrNull("test.key")

            assertThat(result).isEqualTo("test-value")
        }
    }

    @Nested
    inner class GradlePropertyAsBoolean {

        @Test
        fun `returns true when property is true`() {
            val providers = mockk<ProviderFactory>()
            val provider = mockk<Provider<String>>()
            every { providers.gradleProperty("test.enabled") } returns provider
            every { provider.orNull } returns "true"

            val result = providers.gradlePropertyAsBoolean("test.enabled", false)

            assertThat(result).isTrue()
        }

        @Test
        fun `returns false when property is false`() {
            val providers = mockk<ProviderFactory>()
            val provider = mockk<Provider<String>>()
            every { providers.gradleProperty("test.enabled") } returns provider
            every { provider.orNull } returns "false"

            val result = providers.gradlePropertyAsBoolean("test.enabled", true)

            assertThat(result).isFalse()
        }

        @Test
        fun `returns default when property not set`() {
            val providers = mockk<ProviderFactory>()
            val provider = mockk<Provider<String>>()
            every { providers.gradleProperty("test.enabled") } returns provider
            every { provider.orNull } returns null

            val result = providers.gradlePropertyAsBoolean("test.enabled", true)

            assertThat(result).isTrue()
        }

        @Test
        fun `returns false for invalid boolean value`() {
            val providers = mockk<ProviderFactory>()
            val provider = mockk<Provider<String>>()
            every { providers.gradleProperty("test.enabled") } returns provider
            every { provider.orNull } returns "invalid"

            val result = providers.gradlePropertyAsBoolean("test.enabled", true)

            assertThat(result).isFalse()
        }
    }

    @Nested
    inner class GradlePropertyAsInt {

        @Test
        fun `returns int value when property is valid integer`() {
            val providers = mockk<ProviderFactory>()
            val provider = mockk<Provider<String>>()
            every { providers.gradleProperty("test.count") } returns provider
            every { provider.orNull } returns "42"

            val result = providers.gradlePropertyAsInt("test.count", 0)

            assertThat(result).isEqualTo(42)
        }

        @Test
        fun `returns default when property not set`() {
            val providers = mockk<ProviderFactory>()
            val provider = mockk<Provider<String>>()
            every { providers.gradleProperty("test.count") } returns provider
            every { provider.orNull } returns null

            val result = providers.gradlePropertyAsInt("test.count", 10)

            assertThat(result).isEqualTo(10)
        }

        @Test
        fun `returns default for invalid integer value`() {
            val providers = mockk<ProviderFactory>()
            val provider = mockk<Provider<String>>()
            every { providers.gradleProperty("test.count") } returns provider
            every { provider.orNull } returns "not-a-number"

            val result = providers.gradlePropertyAsInt("test.count", 5)

            assertThat(result).isEqualTo(5)
        }

        @Test
        fun `returns default for empty value`() {
            val providers = mockk<ProviderFactory>()
            val provider = mockk<Provider<String>>()
            every { providers.gradleProperty("test.count") } returns provider
            every { provider.orNull } returns ""

            val result = providers.gradlePropertyAsInt("test.count", 7)

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
    inner class StringConventionFromGradleProperty {

        @Test
        fun `sets convention when property exists`() {
            val property = mockk<Property<String>>(relaxed = true)
            val providers = mockk<ProviderFactory>()
            val provider = mockk<Provider<String>>()
            every { providers.gradleProperty("test.key") } returns provider
            every { provider.orNull } returns "value-from-property"

            property.conventionFromGradleProperty(providers, "test.key")

            verify { property.convention("value-from-property") }
        }

        @Test
        fun `skips convention when property not set`() {
            val property = mockk<Property<String>>(relaxed = true)
            val providers = mockk<ProviderFactory>()
            val provider = mockk<Provider<String>>()
            every { providers.gradleProperty("test.key") } returns provider
            every { provider.orNull } returns null

            property.conventionFromGradleProperty(providers, "test.key")

            verify(exactly = 0) { property.convention(any<String>()) }
        }
    }

    @Nested
    inner class BooleanConventionFromGradleProperty {

        @Test
        fun `sets convention to true from property`() {
            val property = mockk<Property<Boolean>>(relaxed = true)
            val providers = mockk<ProviderFactory>()
            val provider = mockk<Provider<String>>()
            every { providers.gradleProperty("test.enabled") } returns provider
            every { provider.orNull } returns "true"

            property.conventionFromGradleProperty(providers, "test.enabled", false)

            verify { property.convention(true) }
        }

        @Test
        fun `sets convention to default when property not set`() {
            val property = mockk<Property<Boolean>>(relaxed = true)
            val providers = mockk<ProviderFactory>()
            val provider = mockk<Provider<String>>()
            every { providers.gradleProperty("test.enabled") } returns provider
            every { provider.orNull } returns null

            property.conventionFromGradleProperty(providers, "test.enabled", true)

            verify { property.convention(true) }
        }
    }

    @Nested
    inner class IntConventionFromGradleProperty {

        @Test
        fun `sets convention from property value`() {
            val property = mockk<Property<Int>>(relaxed = true)
            val providers = mockk<ProviderFactory>()
            val provider = mockk<Provider<String>>()
            every { providers.gradleProperty("test.count") } returns provider
            every { provider.orNull } returns "99"

            property.conventionFromGradleProperty(providers, "test.count", 0)

            verify { property.convention(99) }
        }

        @Test
        fun `sets convention to default when property not set`() {
            val property = mockk<Property<Int>>(relaxed = true)
            val providers = mockk<ProviderFactory>()
            val provider = mockk<Provider<String>>()
            every { providers.gradleProperty("test.count") } returns provider
            every { provider.orNull } returns null

            property.conventionFromGradleProperty(providers, "test.count", 25)

            verify { property.convention(25) }
        }
    }
}
