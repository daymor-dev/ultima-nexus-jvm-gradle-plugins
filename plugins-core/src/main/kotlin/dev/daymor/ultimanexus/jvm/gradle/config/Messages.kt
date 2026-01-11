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

package dev.daymor.ultimanexus.jvm.gradle.config

object Messages {
    const val VERSION_CATALOG_NOT_FOUND =
        "Version catalog 'libs' not found. Add a gradle/libs.versions.toml file to your project."

    fun libraryNotFound(name: String) =
        "Library '$name' not found in version catalog 'libs'. " +
            "Add it to your gradle/libs.versions.toml or use a bundle plugin with fallback versions."

    fun versionNotFound(name: String) =
        "Version '$name' not found in version catalog 'libs'. Add it to your gradle/libs.versions.toml."

    const val CHECK_ARTIFACT_NAME_REQUIRED =
        "Property 'checkArtifactName' is required but not set. " +
            "Add 'checkArtifactName=your-check-artifact' to gradle.properties or gradle/shared.properties."

    const val WARN_DISCOURAGED_DEPENDENCY = "WARN: Discouraged dependency notation:"

    fun projectNotFound(name: String, rootDir: String) =
        "Project directory '$name' not found in $rootDir"

    fun checkArtifactNotResolved(name: String) =
        "Check artifact '$name' not found in resolved dependencies"
}
