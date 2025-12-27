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
import java.util.regex.Pattern

class RegexFormatterStep {

    data class RegexRule(
        private val regex: Pattern,
        private val replacement: String,
    ) : Serializable {
        companion object {
            private const val serialVersionUID: Long = 2669352723977921463L
        }

        fun toFormatter(): FormatterFunc {
            return FormatterFunc { input ->
                regex.matcher(input).replaceAll(replacement)
            }
        }
    }

    data class MultiRule(val rules: List<RegexRule>) : Serializable {
        companion object {
            private const val serialVersionUID: Long = 3448591601212951357L
        }

        fun toFormatter(): FormatterFunc {
            return FormatterFunc { input ->
                rules.fold(input) { acc, rule -> rule.toFormatter().apply(acc) }
            }
        }
    }

    companion object {
        fun create(
            name: String,
            regex: String,
            replacement: String,
        ): FormatterStep {
            return FormatterStep.create(
                name,
                RegexRule(Pattern.compile(regex), replacement),
            ) { state ->
                state.toFormatter()
            }
        }

        fun create(
            name: String,
            vararg rules: Pair<String, String>,
        ): FormatterStep {
            val compiledRules =
                rules.map { (regex, replacement) ->
                    RegexRule(Pattern.compile(regex), replacement)
                }
            return FormatterStep.create(name, MultiRule(compiledRules)) { state
                ->
                state.toFormatter()
            }
        }
    }
}
