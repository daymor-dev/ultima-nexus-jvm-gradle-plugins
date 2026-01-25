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

class JavaImportOrderStep(
    private val samePackageDepth: Int,
    private val standardPackageRegex: Regex,
    private val specialImportsRegex: Regex,
) : java.io.Serializable {

    companion object {
        private const val serialVersionUID: Long = 1L

        private val IMPORT_PATTERN = Regex("""^\s*import\s+(static\s+)?([^;]+);.*$""")
        private val PACKAGE_PATTERN = Regex("""^\s*package\s+([^;]+);.*$""")

        @JvmStatic
        @JvmOverloads
        fun create(
            samePackageDepth: Int = 3,
            standardPackageRegex: String = """^java\.""",
            specialImportsRegex: String = """^(javax|jakarta)\.""",
        ): FormatterStep {
            return FormatterStep.create(
                "JavaImportOrderStep",
                JavaImportOrderStep(
                    samePackageDepth,
                    Regex(standardPackageRegex),
                    Regex(specialImportsRegex),
                ),
                JavaImportOrderStep::toFormatter,
            )
        }
    }

    fun toFormatter(): FormatterFunc {
        return FormatterFunc { unixStr ->
            val lines = unixStr.split('\n')

            val packageName = lines
                .firstOrNull { PACKAGE_PATTERN.matches(it) }
                ?.let { PACKAGE_PATTERN.matchEntire(it)?.groupValues?.get(1)?.trim() }

            val samePackagePrefix = packageName?.let { extractPackagePrefix(it, samePackageDepth) }

            val firstImportIndex = lines.indexOfFirst { IMPORT_PATTERN.matches(it) }
            if (firstImportIndex == -1) {
                return@FormatterFunc unixStr
            }

            val lastImportIndex = lines.indexOfLast { IMPORT_PATTERN.matches(it) }

            val importLines = lines.subList(firstImportIndex, lastImportIndex + 1)
                .filter { IMPORT_PATTERN.matches(it) }
                .map { parseImport(it) }

            if (importLines.isEmpty()) {
                return@FormatterFunc unixStr
            }

            val groupedImports = importLines.groupBy { classifyImport(it, samePackagePrefix) }

            val sortedImports = buildSortedImports(groupedImports)

            val beforeImports = lines.subList(0, firstImportIndex)
            val afterImports = lines.subList(lastImportIndex + 1, lines.size)

            (beforeImports + sortedImports + afterImports).joinToString("\n")
        }
    }

    private fun extractPackagePrefix(packageName: String, depth: Int): String {
        val parts = packageName.split('.')
        return if (parts.size >= depth) {
            parts.take(depth).joinToString(".")
        } else {
            packageName
        }
    }

    private fun parseImport(line: String): ImportStatement {
        val match = IMPORT_PATTERN.matchEntire(line)
            ?: throw IllegalArgumentException("Invalid import statement: $line")
        val isStatic = match.groupValues[1].isNotBlank()
        val importPath = match.groupValues[2].trim()
        return ImportStatement(isStatic, importPath, line.trim())
    }

    private fun classifyImport(import: ImportStatement, samePackagePrefix: String?): ImportGroup {
        if (import.isStatic) {
            return ImportGroup.STATIC
        }

        val path = import.importPath

        if (samePackagePrefix != null && path.startsWith("$samePackagePrefix.")) {
            return ImportGroup.SAME_PACKAGE
        }

        if (standardPackageRegex.containsMatchIn(path)) {
            return ImportGroup.STANDARD_JAVA
        }

        if (specialImportsRegex.containsMatchIn(path)) {
            return ImportGroup.SPECIAL_IMPORTS
        }

        return ImportGroup.THIRD_PARTY
    }

    private fun buildSortedImports(groupedImports: Map<ImportGroup, List<ImportStatement>>): List<String> {
        val result = mutableListOf<String>()

        ImportGroup.entries.forEach { group ->
            val imports = groupedImports[group]
            if (!imports.isNullOrEmpty()) {
                if (result.isNotEmpty()) {
                    result.add("")
                }
                imports
                    .sortedBy { it.importPath }
                    .forEach { result.add(it.originalLine) }
            }
        }

        return result
    }

    private enum class ImportGroup {
        STANDARD_JAVA,
        SPECIAL_IMPORTS,
        THIRD_PARTY,
        SAME_PACKAGE,
        STATIC,
    }

    private data class ImportStatement(
        val isStatic: Boolean,
        val importPath: String,
        val originalLine: String,
    )
}
