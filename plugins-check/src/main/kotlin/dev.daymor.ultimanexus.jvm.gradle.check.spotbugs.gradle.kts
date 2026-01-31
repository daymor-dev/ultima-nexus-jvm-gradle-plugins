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

import com.github.spotbugs.snom.Confidence
import com.github.spotbugs.snom.Effort
import com.github.spotbugs.snom.SpotBugsTask
import dev.daymor.ultimanexus.jvm.gradle.config.Defaults
import dev.daymor.ultimanexus.jvm.gradle.config.PropertyKeys
import dev.daymor.ultimanexus.jvm.gradle.util.CheckArtifactUtils.createCheckConfiguration
import dev.daymor.ultimanexus.jvm.gradle.util.CheckArtifactUtils.resolveCheckJarOrNull
import dev.daymor.ultimanexus.jvm.gradle.util.DependencyUtils.getLibsCatalogOrNull
import dev.daymor.ultimanexus.jvm.gradle.util.DependencyUtils.getLibraryOrNull
import dev.daymor.ultimanexus.jvm.gradle.util.PropertyUtils.conventionFromProperty
import dev.daymor.ultimanexus.jvm.gradle.util.PropertyUtils.findPropertyOrNull
import dev.daymor.ultimanexus.jvm.gradle.util.TaskConfigUtils.configureCheckTaskWithJavaPlugin
import org.gradle.api.artifacts.Configuration

/**
 * Convention plugin for SpotBugs static analysis.
 *
 * Configuration via spotbugsConfig extension:
 * ```kotlin
 * spotbugsConfig {
 *     ignoreFailures = false
 *     showStackTraces = true
 *     showProgress = true
 *     effort = "MAX"
 *     reportLevel = "LOW"
 *     excludeFilterFile = "config/spotbugs/exclude-filter.xml"
 * }
 * ```
 *
 * Or via gradle.properties:
 * ```properties
 * spotbugs.ignoreFailures = false
 * spotbugs.showStackTraces = true
 * spotbugs.showProgress = true
 * spotbugs.effort = MAX
 * spotbugs.reportLevel = LOW
 * spotbugs.excludeFilterFile = config/spotbugs/exclude-filter.xml
 * ```
 */
plugins {
    java
    id("com.github.spotbugs")
    id("dev.daymor.ultimanexus.jvm.gradle.base.lifecycle")
}

interface SpotbugsConfigExtension {
    val ignoreFailures: Property<Boolean>
    val showStackTraces: Property<Boolean>
    val showProgress: Property<Boolean>
    val effort: Property<String>
    val reportLevel: Property<String>
    val excludeFilterFile: Property<String>
}

val spotbugsConfig = extensions.create<SpotbugsConfigExtension>("spotbugsConfig")

spotbugsConfig.ignoreFailures.conventionFromProperty(project, PropertyKeys.SpotBugs.IGNORE_FAILURES, false)
spotbugsConfig.showStackTraces.conventionFromProperty(project, PropertyKeys.SpotBugs.SHOW_STACK_TRACES, true)
spotbugsConfig.showProgress.conventionFromProperty(project, PropertyKeys.SpotBugs.SHOW_PROGRESS, true)
spotbugsConfig.effort.convention(project.findPropertyOrNull(PropertyKeys.SpotBugs.EFFORT) ?: Defaults.SPOTBUGS_EFFORT)
spotbugsConfig.reportLevel.convention(project.findPropertyOrNull(PropertyKeys.SpotBugs.REPORT_LEVEL) ?: Defaults.SPOTBUGS_REPORT_LEVEL)
spotbugsConfig.excludeFilterFile.conventionFromProperty(project, PropertyKeys.SpotBugs.EXCLUDE_FILTER_FILE)

val libs: VersionCatalog? = getLibsCatalogOrNull(project)

val checkArtifactConfig: Configuration by lazy {
    createCheckConfiguration(Defaults.ConfigurationName.SPOTBUGS_CHECK_ARTIFACT, libs)
}

val checkJarFile: File? by lazy {
    checkArtifactConfig.resolveCheckJarOrNull()
}

spotbugs {
    ignoreFailures = spotbugsConfig.ignoreFailures.get()
    showStackTraces = spotbugsConfig.showStackTraces.get()
    showProgress = spotbugsConfig.showProgress.get()
    effort = Effort.valueOf(spotbugsConfig.effort.get().uppercase())
    reportLevel = Confidence.valueOf(spotbugsConfig.reportLevel.get().uppercase())

    val customExcludeFilterFile = spotbugsConfig.excludeFilterFile.orNull
    when {
        customExcludeFilterFile != null -> excludeFilter = file(customExcludeFilterFile)
        checkJarFile != null -> {
            val filterFile = zipTree(checkJarFile!!)
                .matching { include("spotbugs-filter.xml") }
                .singleOrNull()
            if (filterFile != null) {
                excludeFilter = filterFile
            }
        }
        else -> {}
    }
}

tasks.withType<SpotBugsTask> {
    group = Defaults.TaskGroup.VERIFICATION_OTHER
    reports.create("html") { required = true }
    mustRunAfter(tasks.withType<Pmd>())
}

project.configureCheckTaskWithJavaPlugin("spotbugsMain")

val spotbugsAnnotations: String = "spotbugs-annotations"

libs?.let { catalog ->
    getLibraryOrNull(catalog, spotbugsAnnotations)?.let { spotbugsLib ->
        dependencies {
            compileOnly(spotbugsLib)
            testCompileOnly(spotbugsLib)
        }
    }
}
