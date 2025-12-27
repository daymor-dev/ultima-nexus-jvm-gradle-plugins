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

package dev.daymor.ultimanexus.jvm.gradle.report

plugins {
    java
    `jacoco-report-aggregation`
    id("dev.daymor.ultimanexus.jvm.gradle.base.lifecycle")
}

/**
 * Extension to configure code coverage settings.
 */
interface CodeCoverageConfigExtension {
    /** Patterns for classes to exclude from coverage reports. Default: generated classes */
    val excludePatterns: ListProperty<String>
}

val codeCoverageConfig = extensions.create<CodeCoverageConfigExtension>("codeCoverageConfig")

// Read from gradle.properties with defaults
val defaultExcludes = listOf("**/**_.class", "**/**MapperImpl.class", "**/**Application.class")
val excludesFromProps = providers.gradleProperty("codeCoverage.excludePatterns").orNull
    ?.split(",")
    ?.map { it.trim() }
    ?: defaultExcludes

// Set defaults
codeCoverageConfig.excludePatterns.convention(excludesFromProps)

listOf("jacocoTestReport", "jacocoTestCoverageVerification").forEach { taskName
    ->
    tasks.named(taskName) { group = "verification.other" }
}

configurations.aggregateCodeCoverageReportResults {
    extendsFrom(configurations["internal"])
}

val exclude: List<String>
    get() = codeCoverageConfig.excludePatterns.get()

fun excludeFiles(classDirectories: ConfigurableFileCollection) {
    classDirectories.setFrom(
        files(
            classDirectories.files.map { fileTree(it) { setExcludes(exclude) } }
        )
    )
}

fun configureReport(report: JacocoCoverageReport) {
    report.reportTask {
        group = "reporting"
        excludeFiles(classDirectories)
    }
}

reporting {
    reports {
        val testCodeCoverageReport by
            getting(JacocoCoverageReport::class) { configureReport(this) }

        val integrationTestCodeCoverageReport =
            findByName("integrationTestCodeCoverageReport")
                as? JacocoCoverageReport
                ?: register("integrationTestCodeCoverageReport", JacocoCoverageReport::class) {
                    testSuiteName = "integrationTest"
                }.get()
        configureReport(integrationTestCodeCoverageReport)

        val functionalTestCodeCoverageReport =
            findByName("functionalTestCodeCoverageReport")
                as? JacocoCoverageReport
                ?: register("functionalTestCodeCoverageReport", JacocoCoverageReport::class) {
                    testSuiteName = "functionalTest"
                }.get()
        configureReport(functionalTestCodeCoverageReport)

        val allTestCodeCoverageReport =
            findByName("allTestCodeCoverageReport")
                as? JacocoCoverageReport
                ?: register("allTestCodeCoverageReport", JacocoCoverageReport::class) {
                    testSuiteName = "allTest"
                }.get()
        allTestCodeCoverageReport.reportTask {
            group = "reporting"
            excludeFiles(classDirectories)

            classDirectories.setFrom(
                testCodeCoverageReport.reportTask.get().classDirectories,
                integrationTestCodeCoverageReport.reportTask.get().classDirectories,
                functionalTestCodeCoverageReport.reportTask.get().classDirectories,
            )

            executionData.setFrom(
                testCodeCoverageReport.reportTask.get().executionData,
                integrationTestCodeCoverageReport.reportTask.get().executionData,
                functionalTestCodeCoverageReport.reportTask.get().executionData,
            )
        }
    }
}
