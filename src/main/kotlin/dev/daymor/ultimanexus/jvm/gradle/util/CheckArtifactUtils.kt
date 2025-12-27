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
import java.util.zip.ZipFile
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.VersionCatalog

object CheckArtifactUtils {

    /** Default artifact name for check configuration files. */
    const val DEFAULT_CHECK_ARTIFACT_NAME = "ultima-nexus-jvm-check"

    /**
     * Gets the check artifact name from gradle.properties or uses the default.
     * Configure via `checkArtifactName` property in gradle.properties.
     *
     * @param project The Gradle project
     * @return The check artifact name to use
     */
    fun getCheckArtifactName(project: Project): String =
        project.providers.gradleProperty("checkArtifactName").orNull
            ?: DEFAULT_CHECK_ARTIFACT_NAME

    fun Project.createCheckConfiguration(
        name: String,
        libs: VersionCatalog,
    ): Configuration {
        val artifactName = getCheckArtifactName(this)
        return configurations
            .create(name) {
                isCanBeConsumed = false
                isCanBeResolved = true
            }
            .also {
                dependencies.add(
                    name,
                    DependencyUtils.getLibrary(libs, artifactName),
                )
            }
    }

    fun Configuration.resolveCheckJar(project: Project): File {
        val artifactName = getCheckArtifactName(project)
        return resolve().first { it.name.contains(artifactName) }
    }

    @Deprecated("Use resolveCheckJar(Project) instead", ReplaceWith("resolveCheckJar(project)"))
    fun Configuration.resolveCheckJar(): File =
        resolve().first { it.name.contains(DEFAULT_CHECK_ARTIFACT_NAME) }

    fun readFromJar(jarFile: File, entryName: String): String =
        ZipFile(jarFile).use { zip ->
            zip.getEntry(entryName)?.let { entry ->
                zip.getInputStream(entry).bufferedReader().readText()
            } ?: error("Entry $entryName not found in $jarFile")
        }
}
