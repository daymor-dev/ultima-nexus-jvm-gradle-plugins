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
import dev.daymor.ultimanexus.jvm.gradle.spotless.JavaImportOrderStep
import dev.daymor.ultimanexus.jvm.gradle.spotless.RegexFormatterStep
import dev.daymor.ultimanexus.jvm.gradle.util.CheckArtifactUtils.createCheckConfiguration
import dev.daymor.ultimanexus.jvm.gradle.util.CheckArtifactUtils.readFromJar
import dev.daymor.ultimanexus.jvm.gradle.util.CheckArtifactUtils.resolveCheckJarOrNull
import dev.daymor.ultimanexus.jvm.gradle.util.DependencyUtils.FallbackVersions
import dev.daymor.ultimanexus.jvm.gradle.util.DependencyUtils.getLibsCatalogOrNull
import dev.daymor.ultimanexus.jvm.gradle.util.DependencyUtils.getVersionOrNull
import dev.daymor.ultimanexus.jvm.gradle.util.PropertyUtils.conventionFromProperty
import dev.daymor.ultimanexus.jvm.gradle.util.PropertyUtils.findPropertyOrNull
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
 *     importSamePackageDepth = 3
 *     importStandardPackageRegex = "^java\\."
 *     importSpecialImportsRegex = "^(javax|jakarta)\\."
 * }
 * ```
 *
 * Or via gradle.properties:
 * ```properties
 * formatJava.formatterConfigFile = config/eclipse-formatter.xml
 * formatJava.licenseHeaderFile = config/license-header.txt
 * formatJava.licenseHeaderText = /* Copyright (C) $YEAR Company */
 * formatJava.importSamePackageDepth = 3
 * formatJava.importStandardPackageRegex = ^java\.
 * formatJava.importSpecialImportsRegex = ^(javax|jakarta)\.
 * company = My Company
 * ```
 */
plugins { id("dev.daymor.ultimanexus.jvm.gradle.check.spotless-base") }

interface FormatJavaConfigExtension {
    val formatterConfigFile: Property<String>
    val licenseHeaderFile: Property<String>
    val licenseHeaderText: Property<String>
    val importSamePackageDepth: Property<Int>
    val importStandardPackageRegex: Property<String>
    val importSpecialImportsRegex: Property<String>
}

val formatJavaConfig = extensions.create<FormatJavaConfigExtension>("formatJavaConfig")

formatJavaConfig.formatterConfigFile.conventionFromProperty(project, PropertyKeys.Format.JAVA_FORMATTER_CONFIG)
formatJavaConfig.licenseHeaderFile.conventionFromProperty(project, PropertyKeys.Format.JAVA_LICENSE_HEADER_FILE)
formatJavaConfig.licenseHeaderText.conventionFromProperty(project, PropertyKeys.Format.JAVA_LICENSE_HEADER_TEXT)
formatJavaConfig.importSamePackageDepth.conventionFromProperty(project, PropertyKeys.Format.IMPORT_SAME_PACKAGE_DEPTH, 3)
formatJavaConfig.importStandardPackageRegex.conventionFromProperty(project, PropertyKeys.Format.IMPORT_STANDARD_PACKAGE_REGEX)
formatJavaConfig.importSpecialImportsRegex.conventionFromProperty(project, PropertyKeys.Format.IMPORT_SPECIAL_IMPORTS_REGEX)

val libs: VersionCatalog? = getLibsCatalogOrNull(project)

val formatConfig: Configuration by lazy {
    createCheckConfiguration("formatCheckArtifact", libs)
}

val checkJarFile: File? by lazy {
    formatConfig.resolveCheckJarOrNull()
}

val companyName: String = project.findPropertyOrNull(PropertyKeys.Format.COMPANY) ?: ""

val rawLicenseHeaderTemplate: String by lazy {
    checkJarFile?.let { readFromJar(it, "apache-license-header.txt") }
        ?: "/*\n * Copyright (C) \$YEAR \$COMPANY.\n */"
}
val defaultLicenseHeader: String by lazy {
    rawLicenseHeaderTemplate.replace($$"$COMPANY", companyName)
}

val eclipseVersion = libs?.let { getVersionOrNull(it, "eclipse-jdt") } ?: FallbackVersions.ECLIPSE_JDT

spotless.java {
    addStep(
        JavaImportOrderStep.create(
            samePackageDepth = formatJavaConfig.importSamePackageDepth.get(),
            standardPackageRegex = formatJavaConfig.importStandardPackageRegex.getOrElse("^java\\."),
            specialImportsRegex = formatJavaConfig.importSpecialImportsRegex.getOrElse("^(javax|jakarta)\\."),
        )
    )
    removeUnusedImports()

    val customFormatterFile = formatJavaConfig.formatterConfigFile.orNull
    when {
        customFormatterFile != null -> eclipse(eclipseVersion).configFile(file(customFormatterFile))
        checkJarFile != null -> eclipse(eclipseVersion)
            .configFile(
                zipTree(checkJarFile!!)
                    .matching { include("java-formatter.xml") }
                    .singleFile
            )
        else -> eclipse(eclipseVersion)
    }

    addStep(
        RegexFormatterStep.create(
            "javadoc-lines",
            """(\s*\*\s.*)(\r?\n)(\s*\*\s<p>)""" to "$1$2*$2$3",
            """(\s*\*[^\r\n]*)[ \t]+(\r?\n)""" to "$1$2",
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
