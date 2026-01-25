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

import dev.daymor.ultimanexus.jvm.gradle.config.Messages
import dev.daymor.ultimanexus.jvm.gradle.config.UltimaNexusConfig
import java.io.File
import java.util.zip.ZipFile
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.VersionCatalog

object CheckArtifactUtils {

    fun getCheckArtifactNameOrNull(project: Project): String? =
        UltimaNexusConfig.get(project).checkArtifactName.orNull

    fun getCheckArtifactName(project: Project): String =
        getCheckArtifactNameOrNull(project)
            ?: throw GradleException(Messages.CHECK_ARTIFACT_NAME_REQUIRED)

    fun Project.createCheckConfiguration(name: String, libs: VersionCatalog): Configuration {
        val artifactName = getCheckArtifactName(this)
        return configurations.create(name) {
            isCanBeConsumed = false
            isCanBeResolved = true
        }.also { dependencies.add(name, DependencyUtils.getLibrary(libs, artifactName)) }
    }

    fun Configuration.resolveCheckJar(project: Project): File {
        val artifactName = getCheckArtifactName(project)
        return resolve().firstOrNull { it.name.contains(artifactName) }
            ?: throw GradleException(Messages.checkArtifactNotResolved(artifactName))
    }

    fun readFromJar(jarFile: File, entryName: String): String =
        ZipFile(jarFile).use { zip ->
            zip.getEntry(entryName)?.let { entry ->
                zip.getInputStream(entry).bufferedReader().use { it.readText() }
            } ?: error("Entry $entryName not found in $jarFile")
        }
}
