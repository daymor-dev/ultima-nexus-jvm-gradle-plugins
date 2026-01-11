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

import dev.daymor.ultimanexus.jvm.gradle.config.PropertyKeys
import dev.daymor.ultimanexus.jvm.gradle.spotless.RegexFormatterStep
import dev.daymor.ultimanexus.jvm.gradle.util.CheckArtifactUtils.createCheckConfiguration
import dev.daymor.ultimanexus.jvm.gradle.util.CheckArtifactUtils.getCheckArtifactName
import dev.daymor.ultimanexus.jvm.gradle.util.CheckArtifactUtils.readFromJar
import dev.daymor.ultimanexus.jvm.gradle.util.CheckArtifactUtils.resolveCheckJar
import dev.daymor.ultimanexus.jvm.gradle.util.DependencyUtils.getLibsCatalogOrNull
import dev.daymor.ultimanexus.jvm.gradle.util.PropertyUtils.conventionFromGradleProperty
import dev.daymor.ultimanexus.jvm.gradle.util.PropertyUtils.gradlePropertyOrNull
import org.gradle.api.artifacts.Configuration

/**
 * Convention plugin for Java source code formatting using Spotless with Eclipse formatter.
 *
 * Configuration via formatJavaConfig extension:
 * ```kotlin
 * formatJavaConfig {
 *     formatterConfigFile = "config/eclipse-formatter.xml"
 *     licenseHeaderFile = "config/license-header.txt"
 *     licenseHeaderText = "/* Copyright (C) \$YEAR Company */"
 * }
 * ```
 *
 * Or via gradle.properties:
 * ```properties
 * format.java.formatterConfigFile = config/eclipse-formatter.xml
 * format.java.licenseHeaderFile = config/license-header.txt
 * format.java.licenseHeaderText = /* Copyright (C) $YEAR Company */
 * format.company = My Company
 * ```
 */
plugins { id("dev.daymor.ultimanexus.jvm.gradle.check.spotless-base") }

interface FormatJavaConfigExtension {
    val formatterConfigFile: Property<String>
    val licenseHeaderFile: Property<String>
    val licenseHeaderText: Property<String>
}

val formatJavaConfig = extensions.create<FormatJavaConfigExtension>("formatJavaConfig")

formatJavaConfig.formatterConfigFile.conventionFromGradleProperty(providers, PropertyKeys.Format.JAVA_FORMATTER_CONFIG)
formatJavaConfig.licenseHeaderFile.conventionFromGradleProperty(providers, PropertyKeys.Format.JAVA_LICENSE_HEADER_FILE)
formatJavaConfig.licenseHeaderText.conventionFromGradleProperty(providers, PropertyKeys.Format.JAVA_LICENSE_HEADER_TEXT)

val libs: VersionCatalog? = getLibsCatalogOrNull(project)

val formatConfig: Configuration? by lazy {
    libs?.let {
        try {
            createCheckConfiguration("formatCheckArtifact", it)
        } catch (_: Exception) {
            null
        }
    }
}

val checkJarFile: File? by lazy {
    try {
        formatConfig?.resolveCheckJar(project)
    } catch (_: Exception) {
        null
    }
}

val checkArtifactName: String by lazy { getCheckArtifactName(project) }

val companyName: String = providers.gradlePropertyOrNull(PropertyKeys.Format.COMPANY) ?: ""

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
