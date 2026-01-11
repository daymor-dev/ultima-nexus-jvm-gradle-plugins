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

package dev.daymor.ultimanexus.jvm.gradle.task

import java.io.File
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

abstract class PluginApplicationOrderAnalysis : DefaultTask() {

    @get:InputFiles abstract val pluginSrcFolders: ConfigurableFileCollection

    @get:OutputFile abstract val pluginApplicationDiagram: RegularFileProperty

    @TaskAction
    fun analyse() {
        val pluginDependencies =
            pluginSrcFolders
                .flatMap { srcFolder ->
                    findGradleFiles(srcFolder).map { it to srcFolder }
                }
                .groupBy { (file, srcFolder) ->
                    extractPackagePath(file, srcFolder)
                }
                .mapValues { (_, files) ->
                    files.associate { (pluginFile, srcFolder) ->
                        val pluginId =
                            extractFullPluginName(pluginFile, srcFolder)
                        pluginId to extractPluginIds(pluginFile.readText())
                    }
                }
                .filter { it.value.isNotEmpty() }

        val lineBreak = "\n            "
        pluginApplicationDiagram
            .get()
            .asFile
            .writeText(
                """
            @startuml

            ${
                    pluginDependencies.map { (packagePath, pluginIds) ->
                        "package \"$packagePath\" {$lineBreak" +
                                pluginIds.keys.joinToString("") {
                                    "agent \"$it\"$lineBreak"
                                } +
                                "$lineBreak}"
                    }.joinToString(lineBreak)
                }

            ${
                    pluginDependencies.values.flatMap { it.entries }
                        .filter { it.value.isNotEmpty() }
                        .flatMap { (from, to) ->
                            to.map {
                                "\"$from\" --down--> \"$it\"$lineBreak"
                            }
                        }
                        .joinToString(lineBreak)
                }

            @enduml
        """
                    .trimIndent()
            )

        logger.lifecycle(
            "Diagram: ${pluginApplicationDiagram.get().asFile.absolutePath}"
        )
    }

    private fun extractPackagePath(file: File, srcFolder: File): String {
        val relativePath = file.relativeTo(srcFolder).parentFile ?: return ""
        return relativePath.path.replace(File.separatorChar, '.')
    }

    private fun findGradleFiles(directory: File): List<File> {
        return directory
            .walkTopDown()
            .filter { it.isFile && it.name.endsWith(".gradle.kts") }
            .toList()
    }

    private fun extractPluginIds(script: String): List<String> {
        return pluginsBlock(script)
            .lines()
            .filter { it.contains("id(") }
            .mapNotNull { line ->
                val start = line.indexOf("id(\"")
                val end = line.indexOf("\")")
                if (start != -1 && end != -1 && end > start + 4) {
                    line.substring(start + 4, end)
                } else null
            }
    }

    private fun pluginsBlock(script: String): String {
        val pluginsStart = script.indexOf("plugins {")
        if (pluginsStart == -1) return ""
        val closingBrace = script.indexOf("}", pluginsStart)
        return if (closingBrace != -1) script.substring(pluginsStart, closingBrace) else ""
    }

    private fun extractFullPluginName(
        pluginFile: File,
        srcFolder: File,
    ): String {
        val packagePath = extractPackagePath(pluginFile, srcFolder)
        val pluginName = pluginFile.name.replaceFirst(".gradle.kts", "")
        return if (packagePath.isEmpty()) pluginName else "$packagePath.$pluginName"
    }
}
