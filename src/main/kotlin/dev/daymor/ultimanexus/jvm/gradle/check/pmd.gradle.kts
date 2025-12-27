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

import dev.daymor.ultimanexus.jvm.gradle.util.CheckArtifactUtils.createCheckConfiguration
import dev.daymor.ultimanexus.jvm.gradle.util.CheckArtifactUtils.getCheckArtifactName
import dev.daymor.ultimanexus.jvm.gradle.util.CheckArtifactUtils.resolveCheckJar
import dev.daymor.ultimanexus.jvm.gradle.util.DependencyUtils.getVersion
import org.gradle.api.artifacts.Configuration

plugins {
    pmd
    id("dev.daymor.ultimanexus.jvm.gradle.base.lifecycle")
}

/**
 * Extension to configure PMD.
 * Users can provide their own ruleset file instead of the default from
 * ultima-nexus-jvm-check artifact.
 */
interface PmdConfigExtension {
    /**
     * Path to custom PMD ruleset XML file.
     * If set, this file will be used instead of the default from
     * ultima-nexus-jvm-check artifact.
     */
    val ruleSetFile: Property<String>
}

val pmdConfig = extensions.create<PmdConfigExtension>("pmdConfig")

// Read from gradle.properties with defaults
val ruleSetFileFromProps = providers.gradleProperty("pmd.ruleSetFile").orNull

// Set conventions from gradle.properties
if (ruleSetFileFromProps != null) pmdConfig.ruleSetFile.convention(ruleSetFileFromProps)

val libs: VersionCatalog = versionCatalogs.named("libs")

// Lazily create check artifact configuration - only if we need the artifact
val checkArtifactConfig: Configuration? by lazy {
    try {
        createCheckConfiguration("pmdCheckArtifact", libs)
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
    pmd {
        toolVersion = getVersion(libs, "pmd")
        ruleSets = emptyList()

        // Use custom ruleset file if provided, otherwise try artifact, otherwise use rootDir
        val customRuleSetFile = pmdConfig.ruleSetFile.orNull
        when {
            customRuleSetFile != null -> ruleSetFiles = files(customRuleSetFile)
            checkJarFile != null -> ruleSetConfig =
                resources.text.fromArchiveEntry(checkJarFile!!, "pmdRuleset.xml")
            else -> ruleSetFiles =
                files(rootProject.file("$checkArtifactName/src/main/resources/pmdRuleset.xml"))
        }

        threads = (Runtime.getRuntime().availableProcessors() / 2).coerceAtLeast(1)
    }
}

tasks.withType<Pmd> {
    group = "verification.other"
    reports {
        xml.required = false
        html.required = true
    }
    mustRunAfter(tasks.withType<Checkstyle>())
}

// Use plugins.withType to wait for Java plugin to create the pmdMain task
plugins.withType<JavaPlugin> {
    tasks.named("pmdMain") { group = "verification" }

    tasks {
        named("qualityCheck") { dependsOn(tasks.named("pmdMain")) }
        named("qualityGate") { dependsOn(tasks.named("pmdMain")) }
    }
}
