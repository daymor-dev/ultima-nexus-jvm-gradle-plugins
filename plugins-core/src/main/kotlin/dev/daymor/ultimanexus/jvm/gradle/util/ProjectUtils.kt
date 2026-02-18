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

import dev.daymor.ultimanexus.jvm.gradle.config.Defaults
import dev.daymor.ultimanexus.jvm.gradle.config.PropertyKeys
import java.io.File
import org.gradle.api.initialization.Settings

object ProjectUtils {

    fun Settings.includeDir(depth: Int = 1, exclusionPatterns: List<String> = getExclusionPatterns()) {
        rootDir.walk()
            .maxDepth(depth)
            .onEnter { it.isDirectory }
            .filter { dir ->
                val relativePath = dir.toRelativeString(rootDir)
                !isExcluded(relativePath, dir.name, exclusionPatterns) &&
                    File(dir, Defaults.BUILD_GRADLE_KTS).exists() &&
                    rootDir != dir
            }
            .map { it.toRelativeString(rootDir) }
            .forEach {
                if (it.contains(File.separatorChar)) {
                    val folder = it.substringBeforeLast(File.separatorChar)
                    val module = it.substringAfterLast(File.separatorChar)
                    include(":$module")
                    project(":$module").projectDir = File(folder, module)
                } else {
                    include(it)
                }
            }
    }

    private fun Settings.getExclusionPatterns(): List<String> =
        providers.gradleProperty(PropertyKeys.Build.PROJECT_STRUCTURE_EXCLUSIONS)
            .orNull
            ?.split(",")
            ?.map { it.trim() }
            ?.filter { it.isNotEmpty() }
            ?: Defaults.PROJECT_STRUCTURE_EXCLUSIONS

    internal fun isExcluded(relativePath: String, dirName: String, patterns: List<String>): Boolean =
        patterns.any { pattern ->
            when {
                pattern.startsWith("*") -> dirName.endsWith(pattern.removePrefix("*"))
                pattern.contains("/") -> relativePath == pattern.replace("/", File.separator)
                else -> dirName == pattern
            }
        }

}
