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

import com.diffplug.gradle.spotless.SpotlessApply
import com.diffplug.gradle.spotless.SpotlessCheck
import com.diffplug.gradle.spotless.SpotlessTask
import dev.daymor.ultimanexus.jvm.gradle.util.CheckArtifactUtils.DEFAULT_CHECK_ARTIFACT_NAME
import dev.daymor.ultimanexus.jvm.gradle.util.CheckArtifactUtils.createCheckConfiguration
import dev.daymor.ultimanexus.jvm.gradle.util.CheckArtifactUtils.getCheckArtifactName
import dev.daymor.ultimanexus.jvm.gradle.util.CheckArtifactUtils.resolveCheckJar
import dev.daymor.ultimanexus.jvm.gradle.util.DependencyUtils.getVersion
import org.gradle.api.artifacts.Configuration

plugins {
    checkstyle
    id("dev.daymor.ultimanexus.jvm.gradle.base.lifecycle")
}

/**
 * Extension to configure checkstyle.
 * Users can customize configuration files or use their own instead of
 * the defaults from ultima-nexus-jvm-check artifact.
 */
interface CheckstyleConfigExtension {
    /**
     * Path to custom checkstyle.xml configuration file.
     * If set, this file will be used instead of the default from
     * ultima-nexus-jvm-check artifact.
     */
    val configFile: Property<String>

    /** Path to header file for license checks. */
    val headerFile: Property<String>

    /** Path to checkstyle suppressions file. */
    val suppressionsFile: Property<String>

    /** Path to file-level suppressions file. */
    val fileSuppressionsFile: Property<String>
}

val checkstyleConfig = extensions.create<CheckstyleConfigExtension>("checkstyleConfig")

// Read from gradle.properties with defaults
val configFileFromProps = providers.gradleProperty("checkstyle.configFile").orNull
val headerFileFromProps = providers.gradleProperty("checkstyle.headerFile").orNull
val suppressionsFileFromProps = providers.gradleProperty("checkstyle.suppressionsFile").orNull
val fileSuppressionsFileFromProps = providers.gradleProperty("checkstyle.fileSuppressionsFile").orNull

// Set conventions from gradle.properties
if (configFileFromProps != null) checkstyleConfig.configFile.convention(configFileFromProps)
if (headerFileFromProps != null) checkstyleConfig.headerFile.convention(headerFileFromProps)
if (suppressionsFileFromProps != null) checkstyleConfig.suppressionsFile.convention(suppressionsFileFromProps)
if (fileSuppressionsFileFromProps != null) checkstyleConfig.fileSuppressionsFile.convention(fileSuppressionsFileFromProps)

val libs: VersionCatalog = versionCatalogs.named("libs")

// Lazily create check artifact configuration - only if we need the artifact
val checkArtifactConfig: Configuration? by lazy {
    try {
        createCheckConfiguration("checkstyleCheckArtifact", libs)
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
    checkstyle {
        toolVersion = getVersion(libs, "checkstyle")

        // Use custom config file if provided, otherwise try artifact, otherwise use rootDir
        val customConfigFile = checkstyleConfig.configFile.orNull
        when {
            customConfigFile != null -> configFile = file(customConfigFile)
            checkJarFile != null -> config =
                resources.text.fromArchiveEntry(checkJarFile!!, "checkstyle.xml")
            else -> configFile =
                rootProject.file("$checkArtifactName/src/main/resources/checkstyle.xml")
        }

        configProperties =
            mapOf(
                "checkstyle.header.file" to checkstyleConfig.headerFile
                    .getOrElse("$rootDir/checkstyle/headerFile.txt"),
                "checkstyle.suppressions" to checkstyleConfig.suppressionsFile
                    .getOrElse("$rootDir/checkstyle/checkstyle-suppressions.xml"),
                "checkstyle.file.suppressions" to checkstyleConfig.fileSuppressionsFile
                    .getOrElse("$rootDir/checkstyle/checkstyle-file-suppressions.xml"),
            )
        isIgnoreFailures = false
        maxErrors = 0
        maxWarnings = 0
    }
}

tasks.withType<Checkstyle> {
    group = "verification.other"
    reports {
        xml.required = false
        html.required = true
        sarif.required = true
    }
    mustRunAfter(
        tasks.withType<SpotlessTask>(),
        tasks.withType<SpotlessCheck>(),
        tasks.withType<SpotlessApply>(),
    )
}

// Use plugins.withType to wait for Java plugin to create the checkstyleMain task
plugins.withType<JavaPlugin> {
    tasks.named("checkstyleMain") { group = "verification" }

    tasks {
        named("qualityCheck") { dependsOn(tasks.named("checkstyleMain")) }
        named("qualityGate") { dependsOn(tasks.named("checkstyleMain")) }
    }
}
