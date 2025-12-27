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

import java.io.File
import org.gradle.api.Project
import org.gradle.api.initialization.Settings

/** Default directory exclusion patterns for project structure discovery. */
val DEFAULT_EXCLUSION_PATTERNS = listOf(
    "gradle/plugins",
    "*-gradle-plugins"
)

/**
 * Utility functions for project structure discovery and configuration.
 */
object ProjectUtils {

    /**
     * Recursively discovers and includes all subprojects with build.gradle.kts files
     * within the specified depth from the root directory.
     *
     * This function walks the directory tree up to the specified depth, finding all
     * directories containing a build.gradle.kts file, and includes them as Gradle
     * subprojects.
     *
     * Directories matching exclusion patterns are skipped. The default exclusions
     * are `gradle/plugins` and `*-gradle-plugins`, which can be customized via
     * the `projectStructureExclusions` Gradle property (comma-separated list).
     *
     * @param depth The maximum directory depth to search (default: 1)
     * @param exclusionPatterns List of patterns to exclude. Patterns support:
     *   - Exact path match: "gradle/plugins"
     *   - Suffix match with wildcard: "*-gradle-plugins"
     */
    fun Settings.includeDir(
        depth: Int = 1,
        exclusionPatterns: List<String> = getExclusionPatterns()
    ) {
        rootDir
            .walk()
            .maxDepth(depth)
            .onEnter { it.isDirectory }
            .filter { dir ->
                val relativePath = dir.toRelativeString(rootDir)
                !isExcluded(relativePath, dir.name, exclusionPatterns) &&
                    File(dir, "build.gradle.kts").exists() &&
                    rootDir != dir
            }
            .map { it.toRelativeString(rootDir) }
            .forEach {
                if (it.contains(File.separatorChar)) {
                    val folder = it.substringBeforeLast(File.separatorChar)
                    val module = it.substringAfterLast(File.separatorChar)
                    include(":$module")
                    project(":$module").projectDir = File("$folder/$module")
                } else {
                    include(it)
                }
            }
    }

    /**
     * Gets exclusion patterns from settings properties or returns defaults.
     */
    private fun Settings.getExclusionPatterns(): List<String> {
        val customExclusions = providers.gradleProperty("projectStructureExclusions")
            .orNull
            ?.split(",")
            ?.map { it.trim() }
            ?.filter { it.isNotEmpty() }
        return customExclusions ?: DEFAULT_EXCLUSION_PATTERNS
    }

    /**
     * Checks if a directory should be excluded based on exclusion patterns.
     *
     * @param relativePath The path relative to root dir
     * @param dirName The directory name
     * @param patterns The exclusion patterns to check
     * @return true if the directory should be excluded
     */
    private fun isExcluded(
        relativePath: String,
        dirName: String,
        patterns: List<String>
    ): Boolean {
        return patterns.any { pattern ->
            when {
                // Wildcard suffix pattern (e.g., "*-gradle-plugins")
                pattern.startsWith("*") -> {
                    val suffix = pattern.removePrefix("*")
                    dirName.endsWith(suffix)
                }
                // Exact path pattern (e.g., "gradle/plugins")
                pattern.contains("/") -> relativePath == pattern.replace("/", File.separator)
                // Simple directory name match
                else -> dirName == pattern
            }
        }
    }

    /**
     * Aggregates dependencies from all subprojects within a specific project directory.
     *
     * This function finds all modules within the specified project directory and adds
     * them as implementation dependencies to the current project. Useful for creating
     * aggregator modules that bundle multiple submodules.
     *
     * @param projectName The name of the project directory to aggregate from
     * @param depth The maximum directory depth to search (default: 1)
     */
    fun Project.aggregateDir(projectName: String, depth: Int = 1) {

        rootDir
            .listFiles()
            ?.first { it.name == projectName }
            ?.walk()
            ?.maxDepth(depth)
            ?.onEnter { it.isDirectory && rootDir != it }
            ?.filter {
                it.parentFile.name != "gradle" &&
                    File(it, "build.gradle.kts").exists() &&
                    rootDir != it
            }
            ?.map { it.toRelativeString(rootDir) }
            ?.forEach {
                val dependencyHandler = project.dependencies
                if (it.contains(File.separatorChar)) {
                    val module = it.substringAfterLast(File.separatorChar)
                    dependencyHandler.add("implementation", project(":$module"))
                } else {
                    dependencyHandler.add("implementation", project(":$it"))
                }
            }
    }
}
