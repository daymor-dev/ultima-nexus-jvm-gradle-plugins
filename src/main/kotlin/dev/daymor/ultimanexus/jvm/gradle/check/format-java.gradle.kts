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

import dev.daymor.ultimanexus.jvm.gradle.spotless.RegexFormatterStep
import dev.daymor.ultimanexus.jvm.gradle.util.CheckArtifactUtils.createCheckConfiguration
import dev.daymor.ultimanexus.jvm.gradle.util.CheckArtifactUtils.getCheckArtifactName
import dev.daymor.ultimanexus.jvm.gradle.util.CheckArtifactUtils.readFromJar
import dev.daymor.ultimanexus.jvm.gradle.util.CheckArtifactUtils.resolveCheckJar
import org.gradle.api.artifacts.Configuration

plugins { id("dev.daymor.ultimanexus.jvm.gradle.check.spotless-base") }

/**
 * Extension to configure Java formatting.
 * Users can provide their own formatter and license header files instead
 * of the defaults from ultima-nexus-jvm-check artifact.
 */
interface FormatJavaConfigExtension {
    /**
     * Path to custom Eclipse formatter XML file.
     * If set, this file will be used instead of the default from
     * ultima-nexus-jvm-check artifact.
     */
    val formatterConfigFile: Property<String>

    /**
     * Path to custom license header file.
     * If set, this file will be used instead of the default Apache license
     * header from ultima-nexus-jvm-check artifact.
     */
    val licenseHeaderFile: Property<String>

    /**
     * Custom license header text.
     * If set, this text will be used as the license header.
     * Takes precedence over licenseHeaderFile.
     */
    val licenseHeaderText: Property<String>
}

val formatJavaConfig = extensions.create<FormatJavaConfigExtension>("formatJavaConfig")

// Read from gradle.properties with defaults
val formatterConfigFileFromProps = providers.gradleProperty("formatJava.formatterConfigFile").orNull
val licenseHeaderFileFromProps = providers.gradleProperty("formatJava.licenseHeaderFile").orNull
val licenseHeaderTextFromProps = providers.gradleProperty("formatJava.licenseHeaderText").orNull

// Set conventions from gradle.properties
if (formatterConfigFileFromProps != null) formatJavaConfig.formatterConfigFile.convention(formatterConfigFileFromProps)
if (licenseHeaderFileFromProps != null) formatJavaConfig.licenseHeaderFile.convention(licenseHeaderFileFromProps)
if (licenseHeaderTextFromProps != null) formatJavaConfig.licenseHeaderText.convention(licenseHeaderTextFromProps)

val libs: VersionCatalog = versionCatalogs.named("libs")

// Lazily create check artifact configuration - only if we need the artifact
val formatConfig: Configuration? by lazy {
    try {
        createCheckConfiguration("formatCheckArtifact", libs)
    } catch (_: Exception) {
        null
    }
}

val checkJarFile: File? by lazy {
    try {
        formatConfig?.resolveCheckJar(project)
    } catch (_: Exception) {
        null
    }
}

// Get configurable artifact name for fallback path
val checkArtifactName = getCheckArtifactName(project)

val companyName: String = providers.gradleProperty("company").orNull ?: ""

val rawLicenseHeaderTemplate: String by lazy {
    checkJarFile?.let { readFromJar(it, "apache-license-header.txt") }
        ?: rootProject.file("$checkArtifactName/src/main/resources/apache-license-header.txt")
            .takeIf { it.exists() }?.readText()
        ?: "/*\n * Copyright (C) \$YEAR \$COMPANY.\n */"
}
val defaultLicenseHeader: String by lazy {
    rawLicenseHeaderTemplate.replace($$"$COMPANY", companyName)
}

afterEvaluate {
    spotless.java {
        // Use custom formatter config if provided, otherwise try artifact, otherwise use rootDir
        val customFormatterFile = formatJavaConfig.formatterConfigFile.orNull
        when {
            customFormatterFile != null -> eclipse().configFile(file(customFormatterFile))
            checkJarFile != null -> eclipse()
                .configFile(
                    zipTree(checkJarFile!!)
                        .matching { include("java-formatter.xml") }
                        .singleFile
                )
            else -> eclipse()
                .configFile(
                    rootProject.file("$checkArtifactName/src/main/resources/java-formatter.xml")
                )
        }

        addStep(
            RegexFormatterStep.create(
                "javadoc-lines",
                """(\s*\*\s.*)(\r?\n)(\s*\*\s<p>)""" to "$1$2*$2$3",
                """(\s*\*.*?)[ \t]+(\r?\n)""" to "$1$2",
            )
        )

        // Use custom license header if provided, otherwise use default
        val customHeaderText = formatJavaConfig.licenseHeaderText.orNull
        val customHeaderFile = formatJavaConfig.licenseHeaderFile.orNull
        val headerToUse = when {
            customHeaderText != null -> customHeaderText
            customHeaderFile != null -> file(customHeaderFile).readText()
            else -> defaultLicenseHeader
        }
        licenseHeader(headerToUse, "package")
    }
}
