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

package dev.daymor.ultimanexus.jvm.gradle.base

plugins { id("org.gradlex.jvm-dependency-conflict-resolution") }

/**
 * Extension to configure dependency rules.
 * Users can customize the platform and aggregation module paths.
 */
interface DependencyRulesExtension {

    /**
     * The path to the platform module for version management.
     * Defaults to ":versions".
     */
    val platformPath: Property<String>

    /**
     * The path to the aggregation module that provides versions.
     * Defaults to ":aggregation".
     */
    val aggregationPath: Property<String>
}

val dependencyRules = extensions.create<DependencyRulesExtension>("dependencyRules")

// Get default paths from gradle.properties or use conventional defaults
val defaultPlatformPath = providers.gradleProperty("dependencyRules.platformPath")
    .orElse(":versions")
val defaultAggregationPath = providers.gradleProperty("dependencyRules.aggregationPath")
    .orElse(":")

afterEvaluate {
    jvmDependencyConflicts {
        consistentResolution {
            platform(dependencyRules.platformPath.orNull ?: defaultPlatformPath.get())
            providesVersions(
                dependencyRules.aggregationPath.orNull ?: defaultAggregationPath.get()
            )
        }
    }
}
