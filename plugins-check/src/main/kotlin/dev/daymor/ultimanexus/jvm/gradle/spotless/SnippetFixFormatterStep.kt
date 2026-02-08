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

import com.diffplug.spotless.FormatterFunc
import com.diffplug.spotless.FormatterStep
import java.io.Serializable

/**
 * A Spotless formatter step that restores {@code @} symbols inside
 * Javadoc {@code {@snippet}} blocks after the Eclipse JDT formatter
 * incorrectly escapes them to {@code &#64;}.
 *
 * The Eclipse JDT formatter does not recognize {@code {@snippet}}
 * blocks (introduced in Java 18 via JEP 413) and treats lines
 * starting with {@code @} as Javadoc tags, escaping them to HTML
 * entities. This step reverses that escaping within snippet blocks
 * only, leaving all other {@code &#64;} occurrences untouched.
 *
 * @see <a href="https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2071">Eclipse JDT Issue #2071</a>
 */
class SnippetFixFormatterStep private constructor() : Serializable {

    companion object {
        private const val serialVersionUID: Long = 1L

        private const val SNIPPET_START = "{@snippet"
        private const val ESCAPED_AT = "&#64;"
        private const val AT_SYMBOL = "@"

        @JvmStatic
        fun create(): FormatterStep {
            return FormatterStep.create(
                "SnippetFixFormatterStep",
                SnippetFixFormatterStep(),
                SnippetFixFormatterStep::toFormatter,
            )
        }
    }

    fun toFormatter(): FormatterFunc {
        return FormatterFunc { input ->
            if (!input.contains(SNIPPET_START)) {
                return@FormatterFunc input
            }
            fixSnippetBlocks(input)
        }
    }

    private fun fixSnippetBlocks(input: String): String {
        val result = StringBuilder(input.length)
        var i = 0

        while (i < input.length) {
            val snippetStart = input.indexOf(SNIPPET_START, i)
            if (snippetStart == -1) {
                result.append(input, i, input.length)
                break
            }

            result.append(input, i, snippetStart)

            val colonIndex = input.indexOf(':', snippetStart)
            if (colonIndex == -1) {
                result.append(input, snippetStart, input.length)
                break
            }

            val snippetEnd = findMatchingClose(input, snippetStart)
            if (snippetEnd == -1) {
                result.append(input, snippetStart, input.length)
                break
            }

            val snippetContent = input.substring(snippetStart, snippetEnd + 1)
            result.append(snippetContent.replace(ESCAPED_AT, AT_SYMBOL))

            i = snippetEnd + 1
        }

        return result.toString()
    }

    private fun findMatchingClose(input: String, start: Int): Int {
        var depth = 0
        var i = start

        while (i < input.length) {
            when (input[i]) {
                '{' -> depth++
                '}' -> {
                    depth--
                    if (depth == 0) return i
                }
            }
            i++
        }
        return -1
    }
}
