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
import dev.daymor.ultimanexus.jvm.gradle.config.PropertyKeys
import dev.daymor.ultimanexus.jvm.gradle.util.CheckArtifactUtils.createCheckConfiguration
import dev.daymor.ultimanexus.jvm.gradle.util.CheckArtifactUtils.getCheckArtifactNameOrNull
import dev.daymor.ultimanexus.jvm.gradle.util.CheckArtifactUtils.resolveCheckJar
import dev.daymor.ultimanexus.jvm.gradle.util.DependencyUtils.FallbackVersions
import dev.daymor.ultimanexus.jvm.gradle.util.DependencyUtils.getLibsCatalogOrNull
import dev.daymor.ultimanexus.jvm.gradle.util.DependencyUtils.getVersionOrNull
import dev.daymor.ultimanexus.jvm.gradle.util.PropertyUtils.findPropertyOrNull
import dev.daymor.ultimanexus.jvm.gradle.util.TaskConfigUtils.configureCheckTaskWithJavaPlugin
import org.gradle.api.artifacts.Configuration

/**
 * Convention plugin for PMD static code analysis.
 *
 * Configuration via pmdConfig extension:
 * ```kotlin
 * pmdConfig {
 *     ruleSetFile = "config/pmd/ruleset.xml"
 * }
 * ```
 *
 * Or via gradle.properties:
 * ```properties
 * pmd.ruleSetFile = config/pmd/ruleset.xml
 * ```
 */
plugins {
    pmd
    id("dev.daymor.ultimanexus.jvm.gradle.base.lifecycle")
}

interface PmdConfigExtension {
    val ruleSetFile: Property<String>
}

val pmdConfig = extensions.create<PmdConfigExtension>("pmdConfig")

val ruleSetFileFromProps = project.findPropertyOrNull(PropertyKeys.Pmd.RULE_SET_FILE)

if (ruleSetFileFromProps != null) pmdConfig.ruleSetFile.convention(ruleSetFileFromProps)

val libs: VersionCatalog? = getLibsCatalogOrNull(project)

val checkArtifactConfig: Configuration? by lazy {
    libs?.let {
        try {
            createCheckConfiguration(Defaults.ConfigurationName.PMD_CHECK_ARTIFACT, it)
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

val checkArtifactName: String? by lazy { getCheckArtifactNameOrNull(project) }

val defaultPmdVersion = FallbackVersions.PMD

pmd {
    toolVersion = libs?.let { getVersionOrNull(it, "pmd") } ?: defaultPmdVersion
    ruleSets = emptyList()

    val customRuleSetFile = pmdConfig.ruleSetFile.orNull
    when {
        customRuleSetFile != null -> ruleSetFiles = files(customRuleSetFile)
        checkJarFile != null -> ruleSetConfig =
            resources.text.fromArchiveEntry(checkJarFile!!, "pmdRuleset.xml")
        checkArtifactName != null -> ruleSetFiles =
            files(rootProject.file("$checkArtifactName/src/main/resources/pmdRuleset.xml"))
        else -> {
            ruleSets = listOf("category/java/bestpractices.xml")
        }
    }

    threads = (Runtime.getRuntime().availableProcessors() / 2).coerceAtLeast(1)
}

tasks.withType<Pmd> {
    group = Defaults.TaskGroup.VERIFICATION_OTHER
    reports {
        xml.required = false
        html.required = true
    }
    mustRunAfter(tasks.withType<Checkstyle>())
}

project.configureCheckTaskWithJavaPlugin("pmdMain")
