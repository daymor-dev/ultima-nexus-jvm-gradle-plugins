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

package dev.daymor.ultimanexus.jvm.gradle.feature

import dev.daymor.ultimanexus.jvm.gradle.util.ProjectUtils.aggregateDir

plugins {
    java
    id("dev.daymor.ultimanexus.jvm.gradle.base.lifecycle")
    id("dev.daymor.ultimanexus.jvm.gradle.base.dependency-rules")
    id("dev.daymor.ultimanexus.jvm.gradle.check.format-gradle")
    id("dev.daymor.ultimanexus.jvm.gradle.report.code-coverage")
    id("dev.daymor.ultimanexus.jvm.gradle.report.plugin-analysis")
    id("dev.daymor.ultimanexus.jvm.gradle.report.sbom")
    id("dev.daymor.ultimanexus.jvm.gradle.report.test")
}

/**
 * Configuration class for the aggregation plugin.
 * Allows specifying directories to aggregate with their search depth.
 *
 * Example usage in build.gradle.kts:
 * ```kotlin
 * plugins {
 *     id("dev.daymor.ultimanexus.jvm.gradle.feature.aggregation")
 * }
 *
 * aggregation {
 *     directory("my-modules", 5)
 *     directory("example-project", 2)
 * }
 * ```
 */
open class AggregationConfig {
    internal val directories = mutableListOf<Pair<String, Int>>()

    /**
     * Add a directory to aggregate.
     *
     * @param name The name of the directory containing subprojects
     * @param depth The maximum depth to search for build.gradle.kts files (default: 1)
     */
    fun directory(name: String, depth: Int = 1) {
        directories.add(name to depth)
    }
}

val aggregation = extensions.create<AggregationConfig>("aggregation")

afterEvaluate {
    aggregation.directories.forEach { (name, depth) ->
        aggregateDir(name, depth)
    }
}
