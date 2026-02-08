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

class SnippetFixFormatterStepTest {

    @TempDir
    lateinit var tempDir: File

    private fun format(input: String): String {
        val step = SnippetFixFormatterStep.create()
        return step.format(input, File(tempDir, "Test.java"))!!
    }

    @Nested
    inner class SnippetFixing {

        @Test
        fun `reverts escaped at-sign inside snippet block`() {
            val input = """
                /**
                 * {@snippet :
                 * &#64;Override
                 * public void run() {}
                 * }
                 */
            """.trimIndent()

            val expected = """
                /**
                 * {@snippet :
                 * @Override
                 * public void run() {}
                 * }
                 */
            """.trimIndent()

            assertThat(format(input)).isEqualTo(expected)
        }

        @Test
        fun `reverts multiple escaped at-signs inside snippet block`() {
            val input = """
                /**
                 * {@snippet :
                 * &#64;Entity
                 * public class Company {
                 *     &#64;Id
                 *     &#64;GeneratedValue
                 *     private UUID id;
                 *
                 *     &#64;Override
                 *     public UUID getId() {
                 *         return id;
                 *     }
                 * }
                 * }
                 */
            """.trimIndent()

            val expected = """
                /**
                 * {@snippet :
                 * @Entity
                 * public class Company {
                 *     @Id
                 *     @GeneratedValue
                 *     private UUID id;
                 *
                 *     @Override
                 *     public UUID getId() {
                 *         return id;
                 *     }
                 * }
                 * }
                 */
            """.trimIndent()

            assertThat(format(input)).isEqualTo(expected)
        }

        @Test
        fun `leaves escaped at-sign outside snippet blocks unchanged`() {
            val input = """
                /**
                 * Use &#64;Override on methods.
                 *
                 * <pre>
                 * &#64;Entity
                 * public class Foo {}
                 * </pre>
                 */
            """.trimIndent()

            assertThat(format(input)).isEqualTo(input)
        }

        @Test
        fun `handles multiple snippet blocks in one file`() {
            val input = """
                /**
                 * {@snippet :
                 * &#64;Override
                 * public void first() {}
                 * }
                 */
                public void first() {}

                /**
                 * {@snippet :
                 * &#64;Override
                 * public void second() {}
                 * }
                 */
                public void second() {}
            """.trimIndent()

            val expected = """
                /**
                 * {@snippet :
                 * @Override
                 * public void first() {}
                 * }
                 */
                public void first() {}

                /**
                 * {@snippet :
                 * @Override
                 * public void second() {}
                 * }
                 */
                public void second() {}
            """.trimIndent()

            assertThat(format(input)).isEqualTo(expected)
        }

        @Test
        fun `handles nested braces inside snippet block`() {
            val input = """
                /**
                 * {@snippet :
                 * &#64;Override
                 * public void run() {
                 *     if (true) {
                 *         doSomething();
                 *     }
                 * }
                 * }
                 */
            """.trimIndent()

            val expected = """
                /**
                 * {@snippet :
                 * @Override
                 * public void run() {
                 *     if (true) {
                 *         doSomething();
                 *     }
                 * }
                 * }
                 */
            """.trimIndent()

            assertThat(format(input)).isEqualTo(expected)
        }

        @Test
        fun `no-op when file has no snippet blocks`() {
            val input = """
                /**
                 * A simple class.
                 *
                 * @author Malcolm Rozé
                 * @since 0.2.0
                 */
                public class Simple {}
            """.trimIndent()

            assertThat(format(input)).isEqualTo(input)
        }

        @Test
        fun `no-op when file has no escaped at-signs in snippet`() {
            val input = """
                /**
                 * {@snippet :
                 * public void run() {
                 *     System.out.println("hello");
                 * }
                 * }
                 */
            """.trimIndent()

            assertThat(format(input)).isEqualTo(input)
        }
    }

    @Nested
    inner class StepMetadata {

        @Test
        fun `step has correct name`() {
            val step = SnippetFixFormatterStep.create()
            assertThat(step.name).isEqualTo("SnippetFixFormatterStep")
        }
    }

    @Nested
    inner class Serialization {

        @Test
        fun `step instance is serializable`() {
            val instance = SnippetFixFormatterStep::class.java
                .getDeclaredConstructor()
                .apply { isAccessible = true }
                .newInstance()

            assertThat(instance).isInstanceOf(Serializable::class.java)
        }

        @Test
        fun `step can be serialized and deserialized`() {
            val instance = SnippetFixFormatterStep::class.java
                .getDeclaredConstructor()
                .apply { isAccessible = true }
                .newInstance()

            val byteOut = ByteArrayOutputStream()
            ObjectOutputStream(byteOut).use { it.writeObject(instance) }

            val byteIn = ByteArrayInputStream(byteOut.toByteArray())
            val deserialized = ObjectInputStream(byteIn).use {
                it.readObject() as SnippetFixFormatterStep
            }

            val formatter = deserialized.toFormatter()
            val input = """
                /**
                 * {@snippet :
                 * &#64;Override
                 * public void run() {}
                 * }
                 */
            """.trimIndent()
            val expected = """
                /**
                 * {@snippet :
                 * @Override
                 * public void run() {}
                 * }
                 */
            """.trimIndent()

            assertThat(formatter.apply(input)).isEqualTo(expected)
        }
    }
}
