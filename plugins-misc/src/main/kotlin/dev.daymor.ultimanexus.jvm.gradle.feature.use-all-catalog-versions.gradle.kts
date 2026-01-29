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


import dev.daymor.ultimanexus.jvm.gradle.util.DependencyUtils.getLibsCatalogOrNull

/**
 * Plugin that creates dependency constraints from all version catalog entries.
 * Applies strict version constraints for all libraries defined in the catalog.
 *
 * Usage:
 *   plugins {
 *       id("dev.daymor.ultimanexus.jvm.gradle.feature.use-all-catalog-versions")
 *   }
 */
plugins { `java-platform` }

val libs = getLibsCatalogOrNull(project)

dependencies.constraints {
    libs?.let { catalog ->
        val catalogEntries =
            catalog.libraryAliases.map { catalog.findLibrary(it).get().get() }
        catalogEntries
            .filter { it.version != null }
            .forEach {
                api(it) {
                    version { it.version?.let { version -> strictly(version) } }
                }
            }
    }
}
