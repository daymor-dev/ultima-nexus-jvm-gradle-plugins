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

    private const val CHECK_ARTIFACT_KEY = "ultima-nexus-jvm-check"

    fun Project.createCheckConfiguration(configName: String, libs: VersionCatalog?): Configuration =
        configurations.create(configName) {
            isCanBeConsumed = false
            isCanBeResolved = true
        }.also { config ->
            val dependency = libs?.let { DependencyUtils.getLibraryOrNull(it, CHECK_ARTIFACT_KEY) }
                ?: DependencyUtils.Fallbacks.ULTIMA_NEXUS_JVM_CHECK
            dependencies.add(configName, dependency)
        }

    fun Configuration.resolveCheckJarOrNull(): File? =
        runCatching {
            resolve().firstOrNull { it.name.contains(CHECK_ARTIFACT_KEY) }
        }.getOrNull()

    fun readFromJar(jarFile: File, entryName: String): String =
        ZipFile(jarFile).use { zip ->
            zip.getEntry(entryName)?.let { entry ->
                zip.getInputStream(entry).bufferedReader().use { it.readText() }
            } ?: error("Entry $entryName not found in $jarFile")
        }
}
