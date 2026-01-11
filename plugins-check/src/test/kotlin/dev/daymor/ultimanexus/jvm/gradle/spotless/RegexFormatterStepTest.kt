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

package dev.daymor.ultimanexus.jvm.gradle.spotless

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable
import java.util.regex.Pattern

class RegexFormatterStepTest {

    @TempDir
    lateinit var tempDir: File

    private fun format(step: com.diffplug.spotless.FormatterStep, input: String): String =
        step.format(input, File(tempDir, "test.gradle.kts"))!!

    @Nested
    inner class SingleRuleFormatting {

        @Test
        fun `applies simple replacement pattern`() {
            val step = RegexFormatterStep.create("test", "foo", "bar")
            val result = format(step, "hello foo world")

            assertThat(result).isEqualTo("hello bar world")
        }

        @Test
        fun `replaces all occurrences`() {
            val step = RegexFormatterStep.create("test", "a", "b")
            val result = format(step, "aaa")

            assertThat(result).isEqualTo("bbb")
        }

        @Test
        fun `returns unchanged input when no match`() {
            val step = RegexFormatterStep.create("test", "xyz", "abc")
            val result = format(step, "hello world")

            assertThat(result).isEqualTo("hello world")
        }

        @Test
        fun `handles regex special characters`() {
            val step = RegexFormatterStep.create("test", "\\[foo\\]", "[bar]")
            val result = format(step, "value is [foo]")

            assertThat(result).isEqualTo("value is [bar]")
        }

        @Test
        fun `supports capture groups in replacement`() {
            val step = RegexFormatterStep.create("test", "(\\w+)=(\\w+)", "$2=$1")
            val result = format(step, "key=value")

            assertThat(result).isEqualTo("value=key")
        }

        @Test
        fun `handles multiline input`() {
            val step = RegexFormatterStep.create("test", "^line", "row")
            val result = format(step, "line1\nline2\nline3")

            assertThat(result).isEqualTo("row1\nline2\nline3")
        }
    }

    @Nested
    inner class MultiRuleFormatting {

        @Test
        fun `applies multiple rules in sequence`() {
            val step = RegexFormatterStep.create(
                "test",
                "foo" to "bar",
                "bar" to "baz"
            )
            val result = format(step, "foo")

            assertThat(result).isEqualTo("baz")
        }

        @Test
        fun `applies rules in order`() {
            val step = RegexFormatterStep.create(
                "test",
                "a" to "b",
                "b" to "c"
            )
            val result = format(step, "a")

            assertThat(result).isEqualTo("c")
        }

        @Test
        fun `applies multiple independent rules`() {
            val step = RegexFormatterStep.create(
                "test",
                "foo" to "FOO",
                "bar" to "BAR"
            )
            val result = format(step, "foo and bar")

            assertThat(result).isEqualTo("FOO and BAR")
        }
    }

    @Nested
    inner class RegexRuleSerialization {

        @Test
        fun `RegexRule is serializable`() {
            val rule = RegexFormatterStep.RegexRule(Pattern.compile("test"), "replacement")

            assertThat(rule).isInstanceOf(Serializable::class.java)
        }

        @Test
        fun `RegexRule can be serialized and deserialized`() {
            val rule = RegexFormatterStep.RegexRule(Pattern.compile("foo"), "bar")

            val serialized = serializeAndDeserialize(rule)
            val formatter = serialized.toFormatter()

            assertThat(formatter.apply("hello foo")).isEqualTo("hello bar")
        }
    }

    @Nested
    inner class MultiRuleSerialization {

        @Test
        fun `MultiRule is serializable`() {
            val multiRule = RegexFormatterStep.MultiRule(
                listOf(
                    RegexFormatterStep.RegexRule(Pattern.compile("a"), "b"),
                    RegexFormatterStep.RegexRule(Pattern.compile("c"), "d")
                )
            )

            assertThat(multiRule).isInstanceOf(Serializable::class.java)
        }

        @Test
        fun `MultiRule can be serialized and deserialized`() {
            val multiRule = RegexFormatterStep.MultiRule(
                listOf(
                    RegexFormatterStep.RegexRule(Pattern.compile("foo"), "bar"),
                    RegexFormatterStep.RegexRule(Pattern.compile("baz"), "qux")
                )
            )

            val serialized = serializeAndDeserialize(multiRule)
            val formatter = serialized.toFormatter()

            assertThat(formatter.apply("foo baz")).isEqualTo("bar qux")
        }
    }

    @Nested
    inner class FormatterStepCreation {

        @Test
        fun `creates step with correct name`() {
            val step = RegexFormatterStep.create("MyStep", "foo", "bar")

            assertThat(step.name).isEqualTo("MyStep")
        }

        @Test
        fun `creates multi-rule step with correct name`() {
            val step = RegexFormatterStep.create(
                "MultiStep",
                "foo" to "bar",
                "baz" to "qux"
            )

            assertThat(step.name).isEqualTo("MultiStep")
        }
    }

    private inline fun <reified T : Serializable> serializeAndDeserialize(obj: T): T {
        val byteOut = ByteArrayOutputStream()
        ObjectOutputStream(byteOut).use { it.writeObject(obj) }

        val byteIn = ByteArrayInputStream(byteOut.toByteArray())
        return ObjectInputStream(byteIn).use { it.readObject() as T }
    }
}
