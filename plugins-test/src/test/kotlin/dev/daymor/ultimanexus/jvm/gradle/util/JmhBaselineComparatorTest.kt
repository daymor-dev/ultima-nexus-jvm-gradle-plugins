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

package dev.daymor.ultimanexus.jvm.gradle.util

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class JmhBaselineComparatorTest {

    @TempDir
    lateinit var tempDir: File

    private fun result(name: String, mode: String, score: Double): JmhBaselineComparator.BenchmarkResult =
        JmhBaselineComparator.BenchmarkResult(name, mode, score, 0.0)

    @Nested
    inner class Compare {

        @Test
        fun `flags a throughput drop beyond the threshold as a regression`() {
            val baseline = mapOf("a" to result("a", "thrpt", 100.0))
            val current = mapOf("a" to result("a", "thrpt", 70.0))

            val report = JmhBaselineComparator.compare(baseline, current, 20.0)

            assertThat(report.hasRegressions).isTrue()
            val only = report.comparisons.single()
            assertThat(only.status).isEqualTo(JmhBaselineComparator.Status.REGRESSED)
            assertThat(only.percentChange).isEqualTo(-30.0)
        }

        @Test
        fun `treats a throughput drop within the threshold as stable`() {
            val baseline = mapOf("a" to result("a", "thrpt", 100.0))
            val current = mapOf("a" to result("a", "thrpt", 90.0))

            val report = JmhBaselineComparator.compare(baseline, current, 20.0)

            assertThat(report.hasRegressions).isFalse()
            assertThat(report.comparisons.single().status).isEqualTo(JmhBaselineComparator.Status.STABLE)
        }

        @Test
        fun `flags a throughput gain beyond the threshold as an improvement`() {
            val baseline = mapOf("a" to result("a", "thrpt", 100.0))
            val current = mapOf("a" to result("a", "thrpt", 130.0))

            val report = JmhBaselineComparator.compare(baseline, current, 20.0)

            assertThat(report.hasRegressions).isFalse()
            assertThat(report.comparisons.single().status).isEqualTo(JmhBaselineComparator.Status.IMPROVED)
        }

        @Test
        fun `inverts the regression direction for time-based modes`() {
            val baseline = mapOf("a" to result("a", "avgt", 100.0))
            val current = mapOf("a" to result("a", "avgt", 140.0))

            val report = JmhBaselineComparator.compare(baseline, current, 20.0)

            assertThat(report.hasRegressions).isTrue()
            assertThat(report.comparisons.single().status).isEqualTo(JmhBaselineComparator.Status.REGRESSED)
        }

        @Test
        fun `marks a benchmark absent from the baseline as new`() {
            val current = mapOf("a" to result("a", "thrpt", 100.0))

            val report = JmhBaselineComparator.compare(emptyMap(), current, 20.0)

            assertThat(report.hasRegressions).isFalse()
            assertThat(report.comparisons.single().status).isEqualTo(JmhBaselineComparator.Status.NEW)
        }

        @Test
        fun `marks a benchmark absent from the current run as missing`() {
            val baseline = mapOf("a" to result("a", "thrpt", 100.0))

            val report = JmhBaselineComparator.compare(baseline, emptyMap(), 20.0)

            assertThat(report.hasRegressions).isFalse()
            assertThat(report.comparisons.single().status)
                .isEqualTo(JmhBaselineComparator.Status.MISSING_FROM_CURRENT)
        }
    }

    @Nested
    inner class Parse {

        @Test
        fun `parses the JMH JSON result format keyed by benchmark name`() {
            val file = File(tempDir, "CompanyPT.json")
            file.writeText(
                """
                [
                  {
                    "benchmark": "com.example.CompanyPT.benchmarkFindById",
                    "mode": "thrpt",
                    "primaryMetric": { "score": 312.21, "scoreError": 8.4 }
                  }
                ]
                """.trimIndent()
            )

            val parsed = JmhBaselineComparator.parse(file)

            assertThat(parsed).containsKey("com.example.CompanyPT.benchmarkFindById")
            val result = parsed.getValue("com.example.CompanyPT.benchmarkFindById")
            assertThat(result.mode).isEqualTo("thrpt")
            assertThat(result.score).isEqualTo(312.21)
            assertThat(result.scoreError).isEqualTo(8.4)
        }

        @Test
        fun `returns empty for an absent or empty file`() {
            assertThat(JmhBaselineComparator.parse(File(tempDir, "missing.json"))).isEmpty()

            val empty = File(tempDir, "empty.json")
            empty.writeText("")
            assertThat(JmhBaselineComparator.parse(empty)).isEmpty()
        }
    }
}
