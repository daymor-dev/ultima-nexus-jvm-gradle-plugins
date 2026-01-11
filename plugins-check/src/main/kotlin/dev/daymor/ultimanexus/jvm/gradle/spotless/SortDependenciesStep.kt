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
import java.util.*
import java.util.logging.Logger

class SortDependenciesStep : java.io.Serializable {

    companion object {
        private const val serialVersionUID: Long = 2254520889479029838L
        private val logger: Logger = Logger.getLogger(SortDependenciesStep::class.java.name)

        fun create(): FormatterStep {
            return FormatterStep.create(
                "SortDependenciesStep",
                SortDependenciesStep(),
                SortDependenciesStep::toFormatter,
            )
        }
    }

    fun toFormatter(): FormatterFunc {
        return FormatterFunc { unixStr ->
            val lines = unixStr.split('\n')
            val blockStartIndex =
                lines.indexOfFirst {
                    it.startsWith("dependencies {") ||
                        it.startsWith("dependencies.constraints {")
                }
            if (blockStartIndex == -1 || lines[blockStartIndex].contains("}")) {
                unixStr
            } else {
                val blockEndIndex =
                    blockStartIndex +
                        lines.subList(blockStartIndex, lines.size).indexOf("}")
                val subList = getSubList(lines, blockStartIndex, blockEndIndex)
                val declarations =
                    subList.filter { it.contains("(") }.map { parse(it) }
                val comparator =
                    Comparator<DependencyDeclaration> { a, b ->
                        when {
                            a.sourceSet.compareTo(b.sourceSet) != 0 ->
                                a.sourceSet.compareTo(b.sourceSet)

                            a.scope.ordinal != b.scope.ordinal ->
                                a.scope.ordinal.compareTo(b.scope.ordinal)

                            a.isProject && !b.isProject -> -1
                            !a.isProject && b.isProject -> 1
                            else -> a.line.compareTo(b.line)
                        }
                    }

                val sourceSetStartLines =
                    declarations
                        .filter { it.sourceSet.isNotEmpty() }
                        .map {
                            DependencyDeclaration(
                                it.sourceSet,
                                Scope.Api,
                                false,
                                "",
                            )
                        }
                        .distinct()
                val sorted =
                    (declarations + sourceSetStartLines)
                        .sortedWith(comparator)
                        .map { it.line }
                val blockStart = lines.subList(0, blockStartIndex + 1)
                val blockEnd = lines.subList(blockEndIndex, lines.size)

                (blockStart + sorted + blockEnd).joinToString("\n")
            }
        }
    }

    private fun getSubList(
        lines: List<String>,
        blockStartIndex: Int,
        blockEndIndex: Int,
    ) =
        lines.subList(blockStartIndex + 1, blockEndIndex).fold(
            mutableListOf<String>()
        ) { acc, line ->
            if (
                acc.isNotEmpty() &&
                    acc.last().contains("(") &&
                    !acc.last().contains(")")
            ) {
                acc[acc.size - 1] = acc.last() + line.trim()
            } else {
                acc.add(line)
            }
            acc
        }

    private fun parse(line: String): DependencyDeclaration {
        val fullScope = line.trim().substring(0, line.trim().indexOf("("))
        var scope = Scope.Api
        var sourceSet = ""
        val isProject = line.contains("(projects.")
        val isThirdParty = line.contains("(libs.")

        if (!isProject && !isThirdParty) {
            logger.warning("Discouraged dependency notation: ${line.trim()}")
        }

        Scope.entries.forEach { scopeCandidate ->
            if (
                fullScope ==
                    scopeCandidate.name.replaceFirstChar {
                        it.lowercase(Locale.getDefault())
                    }
            ) {
                scope = scopeCandidate
                sourceSet = ""
            }
            if (fullScope.endsWith(scopeCandidate.name)) {
                scope = scopeCandidate
                sourceSet = fullScope.dropLast(scopeCandidate.name.length)
            }
        }
        return DependencyDeclaration(sourceSet, scope, isProject, line)
    }

    private enum class Scope {
        Api,
        Implementation,
        CompileOnlyApi,
        CompileOnly,
        RuntimeOnly,
        ProvidedCompile,
        ProvidedRuntime,
        AnnotationProcessor,
    }

    private data class DependencyDeclaration(
        val sourceSet: String,
        val scope: Scope,
        val isProject: Boolean,
        val line: String,
    )
}
