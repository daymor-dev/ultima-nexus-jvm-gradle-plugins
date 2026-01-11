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

import dev.daymor.ultimanexus.jvm.gradle.config.PropertyKeys

/*
 * Plugin for aggregating JaCoCo code coverage reports across test suites.
 * Auto-discovers all registered JvmTestSuites and creates coverage reports for each.
 *
 * Extension configuration (build.gradle.kts):
 *     codeCoverageConfig {
 *         excludePatterns.set(listOf("...underscore.class", "...MapperImpl.class"))
 *     }
 *
 * Configuration via gradle.properties:
 *     codeCoverage.excludePatterns=<patterns>
 *
 * Tasks (auto-generated per discovered suite):
 *   - testCodeCoverageReport: Coverage for unit tests
 *   - <suiteName>CodeCoverageReport: Coverage for each discovered suite
 *   - allTestCodeCoverageReport: Combined coverage for all tests
 *
 * Prerequisites:
 *   - Requires "internal" configuration for project dependencies
 */
plugins {
    java
    `jacoco-report-aggregation`
    id("dev.daymor.ultimanexus.jvm.gradle.base.lifecycle")
}

interface CodeCoverageConfigExtension {
    val excludePatterns: ListProperty<String>
}

val codeCoverageConfig = extensions.create<CodeCoverageConfigExtension>("codeCoverageConfig")

val defaultExcludes = listOf("**/**_.class", "**/**MapperImpl.class", "**/**Application.class")
val excludesFromProps = providers.gradleProperty(PropertyKeys.CodeCoverage.EXCLUDE_PATTERNS).orNull
    ?.split(",")
    ?.map { it.trim() }
    ?: defaultExcludes

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

// Auto-discover and create coverage reports for all test suites
afterEvaluate {
    val suiteReports = mutableListOf<JacocoCoverageReport>()

    reporting {
        reports {
            // Always configure the default test suite
            val testCodeCoverageReport by getting(JacocoCoverageReport::class) {
                configureReport(this)
            }
            suiteReports.add(testCodeCoverageReport)

            // Auto-discover other JvmTestSuites
            testing.suites.withType<JvmTestSuite>().forEach { suite ->
                val suiteName = suite.name
                if (suiteName != "test" && suiteName != "allTest") {
                    val reportName = "${suiteName}CodeCoverageReport"
                    val report = findByName(reportName) as? JacocoCoverageReport
                        ?: register(reportName, JacocoCoverageReport::class) {
                            testSuiteName = suiteName
                        }.get()
                    configureReport(report)
                    suiteReports.add(report)
                }
            }

            // Create aggregated report for all tests
            val allTestCodeCoverageReport = findByName("allTestCodeCoverageReport")
                as? JacocoCoverageReport
                ?: register("allTestCodeCoverageReport", JacocoCoverageReport::class) {
                    testSuiteName = "allTest"
                }.get()

            allTestCodeCoverageReport.reportTask {
                group = "reporting"
                excludeFiles(classDirectories)

                classDirectories.setFrom(
                    suiteReports.map { it.reportTask.get().classDirectories }
                )

                executionData.setFrom(
                    suiteReports.map { it.reportTask.get().executionData }
                )
            }
        }
    }
}
