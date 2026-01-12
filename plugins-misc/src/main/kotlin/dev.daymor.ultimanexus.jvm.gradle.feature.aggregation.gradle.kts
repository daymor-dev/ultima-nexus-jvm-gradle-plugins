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


import dev.daymor.ultimanexus.jvm.gradle.config.PluginIds
import dev.daymor.ultimanexus.jvm.gradle.util.ProjectUtils.aggregateDir

/*
 * Aggregation plugin for multi-module projects.
 * Aggregates reports and metrics from subprojects into a single location.
 *
 * Report plugins are only applied if subprojects use the corresponding plugin.
 * This is determined by scanning build files for report or bundle plugin references.
 *
 * Included plugins (always):
 *   - java
 *   - dev.daymor.ultimanexus.jvm.gradle.base.lifecycle
 *
 * Included plugins (conditional - per-plugin detection):
 *   - dev.daymor.ultimanexus.jvm.gradle.report.code-coverage (if code-coverage used)
 *   - dev.daymor.ultimanexus.jvm.gradle.report.test (if test report used)
 *   - dev.daymor.ultimanexus.jvm.gradle.report.sbom (if sbom used)
 *   - dev.daymor.ultimanexus.jvm.gradle.report.plugin-analysis (if plugin-analysis used)
 *
 * Usage:
 *   plugins {
 *       id("dev.daymor.ultimanexus.jvm.gradle.feature.aggregation")
 *   }
 *
 *   aggregation {
 *       directory("my-modules", 5)
 *       directory("example-project", 2)
 *   }
 */
plugins {
    java
    id("dev.daymor.ultimanexus.jvm.gradle.base.lifecycle")
}

open class AggregationConfig {
    internal val directories = mutableListOf<Pair<String, Int>>()

    fun directory(name: String, depth: Int = 1) {
        directories.add(name to depth)
    }
}

val aggregation = extensions.create<AggregationConfig>("aggregation")

private val COMPLETE_BUNDLES = listOf(
    "bundle.report",
    "bundle.java-complete",
    "bundle.spring-boot-complete",
    "bundle.ultima-nexus-jvm"
)

data class ReportPluginConfig(
    val pluginId: String,
    val patterns: List<String>
)

private val reportPlugins = listOf(
    ReportPluginConfig(
        PluginIds.Report.CODE_COVERAGE,
        listOf("report.code-coverage") + COMPLETE_BUNDLES
    ),
    ReportPluginConfig(
        PluginIds.Report.TEST,
        listOf("report.test") + COMPLETE_BUNDLES
    ),
    ReportPluginConfig(
        PluginIds.Report.SBOM,
        listOf("report.sbom") + COMPLETE_BUNDLES
    ),
    ReportPluginConfig(
        PluginIds.Report.PLUGIN_ANALYSIS,
        listOf("report.plugin-analysis", "bundle.report")
    )
)

fun collectBuildFileContents(): List<String> {
    return aggregation.directories.flatMap { (name, depth) ->
        val projectDir = rootDir.resolve(name)
        if (!projectDir.exists() || !projectDir.isDirectory) emptyList()
        else projectDir.walk()
            .maxDepth(depth)
            .filter { it.name == "build.gradle.kts" }
            .map { it.readText() }
            .toList()
    }
}

fun findUsedReportPlugins(buildContents: List<String>): Set<String> {
    return reportPlugins
        .filter { config ->
            config.patterns.any { pattern ->
                buildContents.any { it.contains(pattern) }
            }
        }
        .map { it.pluginId }
        .toSet()
}

afterEvaluate {
    val buildContents = collectBuildFileContents()

    findUsedReportPlugins(buildContents).forEach { pluginId ->
        apply(plugin = pluginId)
    }

    aggregation.directories.forEach { (name, depth) ->
        aggregateDir(name, depth)
    }
}
