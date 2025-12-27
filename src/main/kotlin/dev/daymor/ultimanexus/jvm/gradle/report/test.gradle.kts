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
    `test-report-aggregation`
}

configurations.aggregateTestReportResults {
    extendsFrom(configurations["internal"])
}

reporting {
    reports {
        val testAggregateTestReport by
            getting(AggregateTestReport::class) {
                reportTask { group = "reporting" }
            }
        val integrationTestAggregateTestReport by
            creating(AggregateTestReport::class) {
                testSuiteName = "integrationTest"
                reportTask { group = "reporting" }
            }
        val functionalTestAggregateTestReport by
            creating(AggregateTestReport::class) {
                testSuiteName = "functionalTest"
                reportTask { group = "reporting" }
            }
        val allTestAggregateTestReport by
            creating(AggregateTestReport::class) {
                testSuiteName = "allTest"
                reportTask {
                    group = "reporting"

                    testResults.setFrom(
                        testAggregateTestReport.reportTask.get().testResults,
                        integrationTestAggregateTestReport.reportTask
                            .get()
                            .testResults,
                        functionalTestAggregateTestReport.reportTask
                            .get()
                            .testResults,
                    )
                }
            }
    }
}
