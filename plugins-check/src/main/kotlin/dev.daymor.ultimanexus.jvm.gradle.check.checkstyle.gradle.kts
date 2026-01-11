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

import com.diffplug.gradle.spotless.SpotlessApply
import com.diffplug.gradle.spotless.SpotlessCheck
import com.diffplug.gradle.spotless.SpotlessTask
import dev.daymor.ultimanexus.jvm.gradle.config.Defaults
import dev.daymor.ultimanexus.jvm.gradle.config.PropertyKeys
import dev.daymor.ultimanexus.jvm.gradle.util.CheckArtifactUtils.createCheckConfiguration
import dev.daymor.ultimanexus.jvm.gradle.util.CheckArtifactUtils.getCheckArtifactName
import dev.daymor.ultimanexus.jvm.gradle.util.CheckArtifactUtils.resolveCheckJar
import dev.daymor.ultimanexus.jvm.gradle.util.DependencyUtils.FallbackVersions
import dev.daymor.ultimanexus.jvm.gradle.util.DependencyUtils.getLibsCatalogOrNull
import dev.daymor.ultimanexus.jvm.gradle.util.DependencyUtils.getVersionOrNull
import dev.daymor.ultimanexus.jvm.gradle.util.PropertyUtils.conventionFromGradleProperty
import dev.daymor.ultimanexus.jvm.gradle.util.TaskConfigUtils.configureCheckTaskWithJavaPlugin
import org.gradle.api.artifacts.Configuration

/**
 * Convention plugin for Checkstyle code style verification.
 *
 * Configuration via checkstyleConfig extension:
 * ```kotlin
 * checkstyleConfig {
 *     configFile = "config/checkstyle/checkstyle.xml"
 *     headerFile = "config/checkstyle/header.txt"
 *     suppressionsFile = "config/checkstyle/suppressions.xml"
 *     fileSuppressionsFile = "config/checkstyle/file-suppressions.xml"
 * }
 * ```
 *
 * Or via gradle.properties:
 * ```properties
 * checkstyle.configFile = config/checkstyle/checkstyle.xml
 * checkstyle.headerFile = config/checkstyle/header.txt
 * checkstyle.suppressionsFile = config/checkstyle/suppressions.xml
 * checkstyle.fileSuppressionsFile = config/checkstyle/file-suppressions.xml
 * ```
 */
plugins {
    checkstyle
    id("dev.daymor.ultimanexus.jvm.gradle.base.lifecycle")
}

interface CheckstyleConfigExtension {
    val configFile: Property<String>
    val headerFile: Property<String>
    val suppressionsFile: Property<String>
    val fileSuppressionsFile: Property<String>
}

val checkstyleConfig = extensions.create<CheckstyleConfigExtension>("checkstyleConfig")

checkstyleConfig.configFile.conventionFromGradleProperty(providers, PropertyKeys.Checkstyle.CONFIG_FILE)
checkstyleConfig.headerFile.conventionFromGradleProperty(providers, PropertyKeys.Checkstyle.HEADER_FILE)
checkstyleConfig.suppressionsFile.conventionFromGradleProperty(providers, PropertyKeys.Checkstyle.SUPPRESSIONS_FILE)
checkstyleConfig.fileSuppressionsFile.conventionFromGradleProperty(providers, PropertyKeys.Checkstyle.FILE_SUPPRESSIONS_FILE)

val libs: VersionCatalog? = getLibsCatalogOrNull(project)

val checkArtifactConfig: Configuration? by lazy {
    libs?.let {
        try {
            createCheckConfiguration(Defaults.ConfigurationName.CHECKSTYLE_CHECK_ARTIFACT, it)
        } catch (_: Exception) {
            null
        }
    }
}

val checkJarFile: File? by lazy {
    try {
        checkArtifactConfig?.resolveCheckJar(project)
    } catch (_: Exception) {
        null
    }
}

val checkArtifactName: String by lazy { getCheckArtifactName(project) }

val defaultCheckstyleVersion = FallbackVersions.CHECKSTYLE

afterEvaluate {
    checkstyle {
        toolVersion = libs?.let { getVersionOrNull(it, "checkstyle") } ?: defaultCheckstyleVersion

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
    group = Defaults.TaskGroup.VERIFICATION_OTHER
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

project.configureCheckTaskWithJavaPlugin("checkstyleMain")
