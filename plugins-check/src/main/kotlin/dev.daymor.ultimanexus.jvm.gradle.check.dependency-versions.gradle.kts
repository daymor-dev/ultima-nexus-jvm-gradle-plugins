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


/*
 * Plugin for validating dependency version consistency.
 * Ensures all dependencies match platform constraints and reports inconsistencies.
 *
 * Usage:
 *   plugins {
 *       id("dev.daymor.ultimanexus.jvm.gradle.check.dependency-versions")
 *   }
 *
 *   dependencyVersions {
 *       excludes.add("org.junit.jupiter:junit-jupiter-api")
 *   }
 *
 * Tasks:
 *   - checkVersionConsistency: Validates versions match platform constraints
 *   - Report: build/reports/version-consistency.txt
 */
plugins {
    id("dev.daymor.ultimanexus.jvm.gradle.base.lifecycle")
    `java-platform`
}

open class DependencyVersionsConfig {
    val excludes: MutableList<String> = mutableListOf()
}

val dependencyVersions = extensions.create<DependencyVersionsConfig>("dependencyVersions")

val checkVersionConsistency by tasks.registering {
    group = "verification"
    description = "Validates dependency version consistency against platform"

    val reportFile = project.layout.buildDirectory.file("reports/version-consistency.txt")
    outputs.file(reportFile)

    doLast {
        val report = StringBuilder()
        val platformConstraints = configurations.getByName("api").allDependencyConstraints
        val runtimeConfig = configurations.findByName("runtimeClasspath")

        if (runtimeConfig == null) {
            reportFile.get().asFile.apply {
                parentFile.mkdirs()
                writeText("No runtimeClasspath configuration found - skipping version check")
            }
            return@doLast
        }

        val runtimeDeps = runtimeConfig.resolvedConfiguration
            .resolvedArtifacts
            .associate { "${it.moduleVersion.id.group}:${it.moduleVersion.id.name}" to it.moduleVersion.id.version }

        var hasInconsistencies = false
        platformConstraints.forEach { constraint ->
            val key = "${constraint.group}:${constraint.name}"
            if (key !in dependencyVersions.excludes) {
                val runtimeVersion = runtimeDeps[key]
                val constraintVersion = constraint.version
                if (runtimeVersion != null && constraintVersion != null && runtimeVersion != constraintVersion) {
                    report.appendLine("INCONSISTENT: $key - platform: $constraintVersion, runtime: $runtimeVersion")
                    hasInconsistencies = true
                }
            }
        }

        reportFile.get().asFile.apply {
            parentFile.mkdirs()
            writeText(if (hasInconsistencies) report.toString() else "All versions consistent")
        }

        if (hasInconsistencies) {
            throw GradleException("Version inconsistencies found. See ${reportFile.get().asFile}")
        }
    }
}

tasks.named("qualityCheck") { dependsOn(checkVersionConsistency) }
tasks.named("qualityGate") { dependsOn(checkVersionConsistency) }
