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
 * Plugin for aggregating test reports across multiple test suites.
 * Auto-discovers all registered JvmTestSuites and creates aggregate reports for each.
 *
 * Tasks (auto-generated per discovered suite):
 *   - testAggregateTestReport: Aggregates unit test reports
 *   - (suiteName)AggregateTestReport: Aggregates each discovered suite's reports
 *   - allTestAggregateTestReport: Aggregates all test reports
 *
 * Prerequisites:
 *   - Requires "internal" configuration for project dependencies
 */
plugins {
    java
    `test-report-aggregation`
}

configurations.aggregateTestReportResults {
    extendsFrom(configurations["internal"])
}

// Auto-discover and create test reports for all test suites
afterEvaluate {
    val suiteReports = mutableListOf<AggregateTestReport>()

    reporting {
        reports {
            // Always configure the default test suite
            val testAggregateTestReport by getting(AggregateTestReport::class) {
                reportTask { group = "reporting" }
            }
            suiteReports.add(testAggregateTestReport)

            // Auto-discover other JvmTestSuites
            testing.suites.withType<JvmTestSuite>().forEach { suite ->
                val suiteName = suite.name
                if (suiteName != "test" && suiteName != "allTest") {
                    val reportName = "${suiteName}AggregateTestReport"
                    val report = findByName(reportName) as? AggregateTestReport
                        ?: register(reportName, AggregateTestReport::class) {
                            testSuiteName = suiteName
                            reportTask { group = "reporting" }
                        }.get()
                    suiteReports.add(report)
                }
            }

            // Create aggregated report for all tests
            val allTestAggregateTestReport = findByName("allTestAggregateTestReport")
                as? AggregateTestReport
                ?: register("allTestAggregateTestReport", AggregateTestReport::class) {
                    testSuiteName = "allTest"
                }.get()

            allTestAggregateTestReport.reportTask {
                group = "reporting"
                testResults.setFrom(
                    suiteReports.map { it.reportTask.get().testResults }
                )
            }
        }
    }
}
