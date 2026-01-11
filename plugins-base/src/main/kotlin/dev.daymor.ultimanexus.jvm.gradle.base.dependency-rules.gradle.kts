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

import dev.daymor.ultimanexus.jvm.gradle.config.Defaults
import dev.daymor.ultimanexus.jvm.gradle.config.PropertyKeys

/*
 * Convention plugin for JVM dependency conflict resolution.
 * Automatically consumes platform project if it exists.
 *
 * Configuration via dependencyRules extension:
 *   dependencyRules {
 *       platformPath = ":versions"
 *       aggregationPath = ":"
 *       autoConsumePlatform = true
 *   }
 *
 * Or via gradle.properties:
 *   dependencyRules.platformPath = :versions
 *   dependencyRules.aggregationPath = :
 *   dependencyRules.autoConsumePlatform = true
 */
plugins { id("org.gradlex.jvm-dependency-conflict-resolution") }

interface DependencyRulesExtension {
    val platformPath: Property<String>
    val aggregationPath: Property<String>
    val autoConsumePlatform: Property<Boolean>
}

val dependencyRules = extensions.create<DependencyRulesExtension>("dependencyRules")

val defaultPlatformPath = providers.gradleProperty(PropertyKeys.DependencyRules.PLATFORM_PATH)
    .orElse(Defaults.PLATFORM_PATH)
val defaultAggregationPath = providers.gradleProperty(PropertyKeys.DependencyRules.AGGREGATION_PATH)
    .orElse(Defaults.AGGREGATION_PATH)
val defaultAutoConsumePlatform = providers.gradleProperty(PropertyKeys.DependencyRules.AUTO_CONSUME_PLATFORM)
    .map { it.toBoolean() }
    .orElse(true)

afterEvaluate {
    val platformPath = dependencyRules.platformPath.orNull ?: defaultPlatformPath.get()
    val aggregationPath = dependencyRules.aggregationPath.orNull ?: defaultAggregationPath.get()
    val autoConsume = dependencyRules.autoConsumePlatform.orNull ?: defaultAutoConsumePlatform.get()

    if (autoConsume && platformPath != project.path && platformPath != aggregationPath) {
        val platformProject = rootProject.findProject(platformPath)
        if (platformProject != null) {
            val targetConfigs = listOf("implementation", "api")
            configurations.matching { it.name in targetConfigs }.configureEach {
                project.dependencies.add(name, project.dependencies.platform(project.dependencies.project(mapOf("path" to platformPath))))
            }
        }
    }

    jvmDependencyConflicts {
        consistentResolution {
            platform(platformPath)
            providesVersions(aggregationPath)
        }
    }
}
