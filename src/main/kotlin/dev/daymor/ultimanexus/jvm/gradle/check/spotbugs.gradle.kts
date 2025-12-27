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

package dev.daymor.ultimanexus.jvm.gradle.check

import com.github.spotbugs.snom.Confidence
import com.github.spotbugs.snom.Effort
import com.github.spotbugs.snom.SpotBugsTask
import dev.daymor.ultimanexus.jvm.gradle.util.CheckArtifactUtils.createCheckConfiguration
import dev.daymor.ultimanexus.jvm.gradle.util.CheckArtifactUtils.getCheckArtifactName
import dev.daymor.ultimanexus.jvm.gradle.util.CheckArtifactUtils.resolveCheckJar
import dev.daymor.ultimanexus.jvm.gradle.util.DependencyUtils.getLibrary
import org.gradle.api.artifacts.Configuration

plugins {
    java
    id("com.github.spotbugs")
    id("dev.daymor.ultimanexus.jvm.gradle.base.lifecycle")
}

/**
 * Extension to configure SpotBugs plugin settings.
 */
interface SpotbugsConfigExtension {
    /** Whether to ignore SpotBugs failures. Default: false */
    val ignoreFailures: Property<Boolean>
    /** Whether to show stack traces. Default: true */
    val showStackTraces: Property<Boolean>
    /** Whether to show progress. Default: true */
    val showProgress: Property<Boolean>
    /** Analysis effort level (MIN, DEFAULT, MAX). Default: MAX */
    val effort: Property<String>
    /** Minimum confidence level to report (LOW, MEDIUM, HIGH). Default: LOW */
    val reportLevel: Property<String>
    /**
     * Path to custom excludeFilter file.
     * If set, this file will be used instead of the default from
     * ultima-nexus-jvm-check artifact.
     */
    val excludeFilterFile: Property<String>
}

val spotbugsConfig = extensions.create<SpotbugsConfigExtension>("spotbugsConfig")

// Read from gradle.properties with defaults
val ignoreFailuresFromProps = providers.gradleProperty("spotbugs.ignoreFailures").orNull?.toBoolean() ?: false
val showStackTracesFromProps = providers.gradleProperty("spotbugs.showStackTraces").orNull?.toBoolean() ?: true
val showProgressFromProps = providers.gradleProperty("spotbugs.showProgress").orNull?.toBoolean() ?: true
val effortFromProps = providers.gradleProperty("spotbugs.effort").orNull ?: "MAX"
val reportLevelFromProps = providers.gradleProperty("spotbugs.reportLevel").orNull ?: "LOW"
val excludeFilterFileFromProps = providers.gradleProperty("spotbugs.excludeFilterFile").orNull

// Set defaults
spotbugsConfig.ignoreFailures.convention(ignoreFailuresFromProps)
spotbugsConfig.showStackTraces.convention(showStackTracesFromProps)
spotbugsConfig.showProgress.convention(showProgressFromProps)
spotbugsConfig.effort.convention(effortFromProps)
spotbugsConfig.reportLevel.convention(reportLevelFromProps)
if (excludeFilterFileFromProps != null) spotbugsConfig.excludeFilterFile.convention(excludeFilterFileFromProps)

val libs: VersionCatalog = versionCatalogs.named("libs")

// Lazily create check artifact configuration - only if we need the artifact
val checkArtifactConfig: Configuration? by lazy {
    try {
        createCheckConfiguration("spotbugsCheckArtifact", libs)
    } catch (_: Exception) {
        null
    }
}

val checkJarFile: File? by lazy {
    try {
        checkArtifactConfig?.resolveCheckJar(project)
    } catch (_: Exception) {
        null
    }
}

// Get configurable artifact name for fallback path
val checkArtifactName = getCheckArtifactName(project)

afterEvaluate {
    spotbugs {
        ignoreFailures = spotbugsConfig.ignoreFailures.get()
        showStackTraces = spotbugsConfig.showStackTraces.get()
        showProgress = spotbugsConfig.showProgress.get()
        effort = Effort.valueOf(spotbugsConfig.effort.get().uppercase())
        reportLevel = Confidence.valueOf(spotbugsConfig.reportLevel.get().uppercase())

        // Use custom exclude filter if provided, otherwise try artifact, otherwise use rootDir
        val customExcludeFilterFile = spotbugsConfig.excludeFilterFile.orNull
        when {
            customExcludeFilterFile != null -> excludeFilter = file(customExcludeFilterFile)
            checkJarFile != null -> {
                // Extract spotbugs-filter.xml from the check artifact JAR
                val filterFile = zipTree(checkJarFile!!)
                    .matching { include("spotbugs-filter.xml") }
                    .singleOrNull()
                if (filterFile != null) {
                    excludeFilter = filterFile
                }
            }
            else -> {
                val fallbackFile = rootProject.file("$checkArtifactName/src/main/resources/spotbugs-filter.xml")
                if (fallbackFile.exists()) {
                    excludeFilter = fallbackFile
                }
            }
        }
    }
}

tasks.withType<SpotBugsTask> {
    group = "verification.other"
    reports.create("html") { required = true }
    mustRunAfter(tasks.withType<Pmd>())
}

// Use plugins.withType to wait for Java plugin to create the spotbugsMain task
plugins.withType<JavaPlugin> {
    tasks.named("spotbugsMain") { group = "verification" }

    tasks {
        named("qualityCheck") { dependsOn(tasks.named("spotbugsMain")) }
        named("qualityGate") { dependsOn(tasks.named("spotbugsMain")) }
    }
}

val spotbugsAnnotations: String = "spotbugs-annotations"

dependencies {
    compileOnly(getLibrary(libs, spotbugsAnnotations))
    testCompileOnly(getLibrary(libs, spotbugsAnnotations))
}
