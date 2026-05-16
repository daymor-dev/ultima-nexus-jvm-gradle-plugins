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
import dev.daymor.ultimanexus.jvm.gradle.util.SpotBugsFilterMerger
import dev.daymor.ultimanexus.jvm.gradle.util.PropertyUtils.conventionFromProperty
import dev.daymor.ultimanexus.jvm.gradle.util.PropertyUtils.findPropertyOrNull
import dev.daymor.ultimanexus.jvm.gradle.util.TaskConfigUtils.configureAllCheckTasksWithJavaPlugin
import org.gradle.api.artifacts.Configuration
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.jvm.tasks.Jar

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
 *     useAuxClasspath = true
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
 * spotbugs.useAuxClasspath = true
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
    val useAuxClasspath: Property<Boolean>
}

val spotbugsConfig = extensions.create<SpotbugsConfigExtension>("spotbugsConfig")

spotbugsConfig.ignoreFailures.conventionFromProperty(project, PropertyKeys.SpotBugs.IGNORE_FAILURES, false)
spotbugsConfig.showStackTraces.conventionFromProperty(project, PropertyKeys.SpotBugs.SHOW_STACK_TRACES, true)
spotbugsConfig.showProgress.conventionFromProperty(project, PropertyKeys.SpotBugs.SHOW_PROGRESS, true)
spotbugsConfig.effort.convention(project.findPropertyOrNull(PropertyKeys.SpotBugs.EFFORT) ?: Defaults.SPOTBUGS_EFFORT)
spotbugsConfig.reportLevel.convention(project.findPropertyOrNull(PropertyKeys.SpotBugs.REPORT_LEVEL) ?: Defaults.SPOTBUGS_REPORT_LEVEL)
spotbugsConfig.excludeFilterFile.conventionFromProperty(project, PropertyKeys.SpotBugs.EXCLUDE_FILTER_FILE)
spotbugsConfig.useAuxClasspath.conventionFromProperty(project, PropertyKeys.SpotBugs.USE_AUX_CLASSPATH, false)

val libs: VersionCatalog? = getLibsCatalogOrNull(project)

val checkArtifactConfig: Configuration by lazy {
    createCheckConfiguration(Defaults.ConfigurationName.SPOTBUGS_CHECK_ARTIFACT, libs)
}

val checkJarFile: File? by lazy {
    checkArtifactConfig.resolveCheckJarOrNull()
}

val resolvedBaseFilterFile: File? by lazy {
    val customExcludeFilterFile = spotbugsConfig.excludeFilterFile.orNull
    when {
        customExcludeFilterFile != null -> file(customExcludeFilterFile)
        checkJarFile != null -> zipTree(checkJarFile!!)
            .matching { include("spotbugs-filter.xml") }
            .singleOrNull()
        else -> null
    }
}

val generatedFilterDir: Provider<Directory> =
    layout.buildDirectory.dir("classes/java/main/META-INF/spotbugs-filters")

val mergedFilterFile: Provider<RegularFile> =
    layout.buildDirectory.file("spotbugs/merged-exclude-filter.xml")

val mergeSpotBugsFilter = tasks.register("mergeSpotBugsFilter") {
    group = Defaults.TaskGroup.VERIFICATION_OTHER
    description = "Merges the base SpotBugs filter with annotation-processor-generated filter fragments."
    dependsOn(tasks.named("compileJava"))

    val baseFilter: File? = resolvedBaseFilterFile
    val baseXml: String? = baseFilter?.takeIf { it.exists() }?.readText(Charsets.UTF_8)
    val generatedDir: File = generatedFilterDir.get().asFile
    val mergedOut: File = mergedFilterFile.get().asFile

    if (baseFilter != null) {
        inputs.file(baseFilter).withPropertyName("baseFilter")
    }
    inputs.files(project.fileTree(generatedDir) { include("*.xml") })
        .withPropertyName("generatedFilters")
        .optional()
    outputs.file(mergedOut).withPropertyName("mergedFilter")

    doLast {
        val fragments: List<String> = if (generatedDir.isDirectory) {
            generatedDir.walkTopDown()
                .filter { it.isFile && it.extension == "xml" }
                .sortedBy { it.absolutePath }
                .map { it.readText(Charsets.UTF_8) }
                .toList()
        } else {
            emptyList()
        }
        val merged = SpotBugsFilterMerger.merge(baseXml, fragments)
        mergedOut.parentFile.mkdirs()
        mergedOut.writeText(merged, Charsets.UTF_8)
    }
}

spotbugs {
    ignoreFailures = spotbugsConfig.ignoreFailures.get()
    showStackTraces = spotbugsConfig.showStackTraces.get()
    showProgress = spotbugsConfig.showProgress.get()
    effort = Effort.valueOf(spotbugsConfig.effort.get().uppercase())
    reportLevel = Confidence.valueOf(spotbugsConfig.reportLevel.get().uppercase())

    if (resolvedBaseFilterFile != null) {
        excludeFilter = mergedFilterFile.get().asFile
    }
}

tasks.withType<SpotBugsTask> {
    group = Defaults.TaskGroup.VERIFICATION_OTHER
    reports.create("html") { required = true }
    mustRunAfter(tasks.withType<Pmd>())
    dependsOn(mergeSpotBugsFilter)
}

tasks.withType<Jar>().configureEach {
    exclude("META-INF/spotbugs-filters/**")
}

if (spotbugsConfig.useAuxClasspath.get()) {
    val javaExtension = project.the<JavaPluginExtension>()
    tasks.withType<SpotBugsTask>().configureEach {
        val sourceSetName = name.removePrefix("spotbugs")
            .replaceFirstChar { it.lowercase() }
        val sourceSet = javaExtension.sourceSets.findByName(sourceSetName)
            ?: javaExtension.sourceSets.getByName("main")
        auxClassPaths.from(sourceSet.runtimeClasspath)
        auxClassPaths.from(sourceSet.compileClasspath)
    }
}

project.configureAllCheckTasksWithJavaPlugin<SpotBugsTask>()

val spotbugsAnnotations: String = "spotbugs-annotations"

libs?.let { catalog ->
    getLibraryOrNull(catalog, spotbugsAnnotations)?.let { spotbugsLib ->
        dependencies {
            compileOnly(spotbugsLib)
            testCompileOnly(spotbugsLib)
        }
    }
}
