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

import groovy.json.JsonSlurper
import java.io.File

/**
 * Compares a current JMH run against a committed baseline, benchmark
 * by benchmark, and classifies each as improved, stable, regressed,
 * new, or missing.
 *
 * The comparison reads the standard JMH JSON result format (the
 * `-rf json` / `ResultFormatType.JSON` output): an array of objects
 * each carrying a `benchmark` name, a `mode`, and a `primaryMetric`
 * with a `score` and a `scoreError`. Benchmarks are matched by their
 * fully-qualified name; the metric compared is `primaryMetric.score`.
 *
 * The sign of a regression depends on the benchmark mode: for
 * throughput modes higher is better (a drop is a regression), for
 * time-based modes lower is better (a rise is a regression). The
 * comparator normalises this so a [BenchmarkComparison.regressed] flag
 * means "worse than baseline beyond the threshold" regardless of mode.
 */
object JmhBaselineComparator {

    /** Throughput-style JMH modes where a higher score is better. */
    private val HIGHER_IS_BETTER = setOf("thrpt")

    /** A single benchmark's score, parsed from a JMH JSON result. */
    data class BenchmarkResult(val name: String, val mode: String, val score: Double, val scoreError: Double)

    /** The outcome of comparing one benchmark's current score against its baseline. */
    data class BenchmarkComparison(
        val name: String,
        val baselineScore: Double?,
        val currentScore: Double?,
        val percentChange: Double?,
        val regressed: Boolean,
        val improved: Boolean,
        val status: Status,
    )

    /** The classification of a single benchmark comparison. */
    enum class Status { IMPROVED, STABLE, REGRESSED, NEW, MISSING_FROM_CURRENT }

    /** The full comparison report across every benchmark in the baseline and the current run. */
    data class Report(val comparisons: List<BenchmarkComparison>) {
        val regressions: List<BenchmarkComparison> get() = comparisons.filter { it.regressed }
        val hasRegressions: Boolean get() = regressions.isNotEmpty()
    }

    /**
     * Lists the JMH JSON result files directly under a directory,
     * sorted by name. Returns an empty list when the directory is
     * absent.
     */
    fun listResultJsons(directory: File): List<File> {
        if (!directory.isDirectory) {
            return emptyList()
        }
        return directory.listFiles { file -> file.isFile && file.name.endsWith(".json") }
            ?.sortedBy { it.name }
            ?: emptyList()
    }

    /** Parses and merges every JMH result under a directory into a single name-keyed map. */
    fun parseDirectory(directory: File): Map<String, BenchmarkResult> =
        listResultJsons(directory).flatMap { parse(it).entries }.associate { it.key to it.value }

    /** Renders the report as plain text lines (one header line plus one line per benchmark). */
    fun format(report: Report, thresholdPercent: Double): List<String> {
        val lines = mutableListOf("comparison versus baseline (threshold ${thresholdPercent}%):")
        report.comparisons.forEach { comparison ->
            val change = comparison.percentChange?.let { String.format("%+.1f%%", it) } ?: "—"
            lines += "  [${comparison.status}] ${comparison.name}: $change"
        }
        return lines
    }

    /**
     * Parses every benchmark result from a JMH JSON file, keyed by
     * benchmark name. Returns an empty map when the file is absent or
     * empty.
     */
    fun parse(file: File): Map<String, BenchmarkResult> {
        if (!file.isFile || file.length() == 0L) {
            return emptyMap()
        }
        val parsed = JsonSlurper().parse(file)
        if (parsed !is List<*>) {
            return emptyMap()
        }
        return parsed.filterIsInstance<Map<*, *>>()
            .mapNotNull { toResult(it) }
            .associateBy { it.name }
    }

    private fun toResult(entry: Map<*, *>): BenchmarkResult? {
        val name = entry["benchmark"] as? String ?: return null
        val mode = entry["mode"] as? String ?: ""
        val metric = entry["primaryMetric"] as? Map<*, *> ?: return null
        val score = (metric["score"] as? Number)?.toDouble() ?: return null
        val error = (metric["scoreError"] as? Number)?.toDouble() ?: Double.NaN
        return BenchmarkResult(name, mode, score, error)
    }

    /**
     * Compares the current results against the baseline. A benchmark
     * is flagged as regressed when its score moves in the worse
     * direction (per its mode) by more than [thresholdPercent] percent.
     *
     * @param baseline        the committed baseline results, by name
     * @param current         the latest run's results, by name
     * @param thresholdPercent the relative tolerance, e.g. `20.0` for 20%
     */
    fun compare(
        baseline: Map<String, BenchmarkResult>,
        current: Map<String, BenchmarkResult>,
        thresholdPercent: Double,
    ): Report {
        val names = (baseline.keys + current.keys).toSortedSet()
        val comparisons = names.map { name -> compareOne(name, baseline[name], current[name], thresholdPercent) }
        return Report(comparisons)
    }

    private fun compareOne(
        name: String,
        baseline: BenchmarkResult?,
        current: BenchmarkResult?,
        thresholdPercent: Double,
    ): BenchmarkComparison {
        if (baseline == null) {
            return BenchmarkComparison(name, null, current?.score, null, false, false, Status.NEW)
        }
        if (current == null) {
            return BenchmarkComparison(name, baseline.score, null, null, false, false, Status.MISSING_FROM_CURRENT)
        }
        val higherIsBetter = baseline.mode in HIGHER_IS_BETTER
        val percentChange = percentChange(baseline.score, current.score)
        val improvementPercent = if (higherIsBetter) percentChange else -percentChange
        val regressed = improvementPercent < -thresholdPercent
        val improved = improvementPercent > thresholdPercent
        val status = when {
            regressed -> Status.REGRESSED
            improved -> Status.IMPROVED
            else -> Status.STABLE
        }
        return BenchmarkComparison(name, baseline.score, current.score, percentChange, regressed, improved, status)
    }

    private fun percentChange(baseline: Double, current: Double): Double {
        if (baseline == 0.0) {
            return 0.0
        }
        return (current - baseline) / baseline * PERCENT
    }

    private const val PERCENT = 100.0
}
