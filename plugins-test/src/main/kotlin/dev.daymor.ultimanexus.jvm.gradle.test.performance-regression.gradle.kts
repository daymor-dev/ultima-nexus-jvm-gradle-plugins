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
import dev.daymor.ultimanexus.jvm.gradle.util.JmhBaselineComparator

/**
 * Plugin: dev.daymor.ultimanexus.jvm.gradle.test.performance-regression
 *
 * Adds JMH baseline-regression tracking on top of the `performanceTest`
 * suite. Two tasks:
 *
 *   - `benchmarkRegression` compares the latest JMH JSON results in
 *     `build/reports/jmh/` against committed baselines in
 *     `perf-baselines/`, benchmark by benchmark (matched by name, on
 *     `primaryMetric.score`), and reports every improvement / stable /
 *     regression beyond the configured threshold.
 *   - `benchmarkBaselineUpdate` promotes the latest results to the
 *     baseline directory so they can be committed and reviewed.
 *
 * Because absolute JMH scores are machine-dependent, the comparison is
 * advisory by default: regressions are logged as warnings and the build
 * stays green. Set `benchmark.failOnRegression = true` (or
 * `-PbenchmarkFailOnRegression=true`) on a stable, dedicated benchmark
 * host to make `benchmarkRegression` fail the build instead.
 *
 * Configuration:
 * ```kotlin
 * benchmark {
 *     thresholdPercent = 20.0          // relative tolerance
 *     failOnRegression = false         // advisory by default
 *     baselineDirectory = layout.projectDirectory.dir("perf-baselines")
 *     resultDirectory = layout.buildDirectory.dir("reports/jmh")
 * }
 * ```
 */

abstract class BenchmarkRegressionExtension {

    /** Relative tolerance (percent) before a score move counts as a regression / improvement. */
    abstract val thresholdPercent: Property<Double>

    /** When true, `benchmarkRegression` fails the build on any regression; otherwise it only warns. */
    abstract val failOnRegression: Property<Boolean>

    /** Directory holding the committed baseline JMH JSON files. */
    abstract val baselineDirectory: DirectoryProperty

    /** Directory the `performanceTest` run writes its JMH JSON results into. */
    abstract val resultDirectory: DirectoryProperty
}

val benchmark = extensions.create<BenchmarkRegressionExtension>("benchmark")

benchmark.thresholdPercent.convention(
    (providers.gradleProperty("benchmarkThresholdPercent").orNull?.toDoubleOrNull())
        ?: Defaults.Benchmark.DEFAULT_THRESHOLD_PERCENT
)
benchmark.failOnRegression.convention(
    providers.gradleProperty("benchmarkFailOnRegression").map { it.toBoolean() }.orElse(false)
)
benchmark.baselineDirectory.convention(layout.projectDirectory.dir(Defaults.Benchmark.BASELINE_DIRECTORY))
benchmark.resultDirectory.convention(layout.buildDirectory.dir(Defaults.Benchmark.RESULT_DIRECTORY))

val benchmarkRegression = tasks.register("benchmarkRegression") {
    group = Defaults.TaskGroup.VERIFICATION
    description = "Compares the latest JMH results against the committed baselines and reports regressions."

    val thresholdPercent = benchmark.thresholdPercent
    val failOnRegression = benchmark.failOnRegression
    val baselineDir = benchmark.baselineDirectory.map { it.asFile }
    val resultDir = benchmark.resultDirectory.map { it.asFile }

    doLast {
        val threshold = thresholdPercent.get()
        val fail = failOnRegression.get()
        val baselineFolder = baselineDir.get()
        val resultFolder = resultDir.get()

        if (JmhBaselineComparator.listResultJsons(baselineFolder).isEmpty()) {
            logger.lifecycle(
                "benchmarkRegression: no baselines in $baselineFolder — " +
                    "run the benchmarks and `benchmarkBaselineUpdate` to establish one."
            )
            return@doLast
        }
        if (JmhBaselineComparator.listResultJsons(resultFolder).isEmpty()) {
            logger.lifecycle(
                "benchmarkRegression: no JMH results in $resultFolder — " +
                    "run `performanceTest` (with generated PTs enabled, if applicable) first."
            )
            return@doLast
        }

        val baseline = JmhBaselineComparator.parseDirectory(baselineFolder)
        val current = JmhBaselineComparator.parseDirectory(resultFolder)
        val report = JmhBaselineComparator.compare(baseline, current, threshold)

        JmhBaselineComparator.format(report, threshold).forEach { logger.lifecycle("benchmarkRegression: $it") }

        if (report.hasRegressions) {
            val summary = "${report.regressions.size} benchmark(s) regressed beyond ${threshold}% versus the baseline."
            if (fail) {
                throw org.gradle.api.GradleException("benchmarkRegression: $summary")
            }
            logger.warn("benchmarkRegression: $summary (advisory — set benchmark.failOnRegression=true to fail)")
        } else {
            logger.lifecycle("benchmarkRegression: no regressions beyond ${threshold}%.")
        }
    }
}

tasks.register("benchmarkBaselineUpdate") {
    group = Defaults.TaskGroup.VERIFICATION
    description = "Promotes the latest JMH results to the committed baseline directory."

    val baselineDir = benchmark.baselineDirectory.map { it.asFile }
    val resultDir = benchmark.resultDirectory.map { it.asFile }

    doLast {
        val resultFolder = resultDir.get()
        val results = JmhBaselineComparator.listResultJsons(resultFolder)
        if (results.isEmpty()) {
            logger.lifecycle("benchmarkBaselineUpdate: no JMH results in $resultFolder — nothing to promote.")
            return@doLast
        }
        val target = baselineDir.get()
        target.mkdirs()
        results.forEach { result -> result.copyTo(target.resolve(result.name), overwrite = true) }
        logger.lifecycle("benchmarkBaselineUpdate: promoted ${results.size} result file(s) to $target.")
    }
}

tasks.matching { it.name == "performanceTest" }.configureEach {
    finalizedBy(benchmarkRegression)
}
