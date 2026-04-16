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
import dev.daymor.ultimanexus.jvm.gradle.util.CheckArtifactUtils
import dev.daymor.ultimanexus.jvm.gradle.util.CheckArtifactUtils.createCheckConfiguration
import dev.daymor.ultimanexus.jvm.gradle.util.CheckArtifactUtils.resolveCheckJarOrNull
import dev.daymor.ultimanexus.jvm.gradle.util.DependencyUtils.FallbackVersions
import dev.daymor.ultimanexus.jvm.gradle.util.DependencyUtils.getLibsCatalogOrNull
import dev.daymor.ultimanexus.jvm.gradle.util.DependencyUtils.getVersionOrNull
import dev.daymor.ultimanexus.jvm.gradle.util.PmdRulesetMerger
import dev.daymor.ultimanexus.jvm.gradle.util.PropertyUtils.findPropertyOrNull
import dev.daymor.ultimanexus.jvm.gradle.util.TaskConfigUtils.configureAllCheckTasksWithJavaPlugin
import org.gradle.api.artifacts.Configuration
import org.gradle.jvm.tasks.Jar

/**
 * Convention plugin for PMD static code analysis.
 *
 * Supports separate rulesets for main and test source sets.
 * Test source sets (test, integrationTest, functionalTest, performanceTest)
 * use a relaxed ruleset by default.
 *
 * Configuration via pmdConfig extension:
 * ```kotlin
 * pmdConfig {
 *     ruleSetFile = "config/pmd/ruleset.xml"
 *     testRuleSetFile = "config/pmd/ruleset-test.xml"
 * }
 * ```
 *
 * Or via gradle.properties:
 * ```properties
 * pmd.ruleSetFile = config/pmd/ruleset.xml
 * pmd.testRuleSetFile = config/pmd/ruleset-test.xml
 * ```
 */
plugins {
    pmd
    id("dev.daymor.ultimanexus.jvm.gradle.base.lifecycle")
}

interface PmdConfigExtension {
    val ruleSetFile: Property<String>
    val testRuleSetFile: Property<String>
    val useTestRulesetForMain: Property<Boolean>
}

val pmdConfig = extensions.create<PmdConfigExtension>("pmdConfig")

val ruleSetFileFromProps = project.findPropertyOrNull(PropertyKeys.Pmd.RULE_SET_FILE)
val testRuleSetFileFromProps = project.findPropertyOrNull(PropertyKeys.Pmd.TEST_RULE_SET_FILE)
val useTestRulesetForMainFromProps =
    project.findPropertyOrNull(PropertyKeys.Pmd.USE_TEST_RULESET_FOR_MAIN)

if (ruleSetFileFromProps != null) pmdConfig.ruleSetFile.convention(ruleSetFileFromProps)
if (testRuleSetFileFromProps != null) pmdConfig.testRuleSetFile.convention(testRuleSetFileFromProps)
pmdConfig.useTestRulesetForMain.convention(
    useTestRulesetForMainFromProps?.toBooleanStrictOrNull() ?: false
)

val libs: VersionCatalog? = getLibsCatalogOrNull(project)

val checkArtifactConfig: Configuration by lazy {
    createCheckConfiguration(Defaults.ConfigurationName.PMD_CHECK_ARTIFACT, libs)
}

val checkJarFile: File? by lazy {
    checkArtifactConfig.resolveCheckJarOrNull()
}

val defaultPmdVersion = FallbackVersions.PMD

val resolvedBaseRulesetContent: String? by lazy {
    val customRuleSetFile = pmdConfig.ruleSetFile.orNull
    when {
        customRuleSetFile != null -> rootProject.file(customRuleSetFile)
            .takeIf { it.exists() }?.readText(Charsets.UTF_8)
        checkJarFile != null -> CheckArtifactUtils.readFromJar(checkJarFile!!, "pmdRuleset.xml")
        else -> null
    }
}

val resolvedTestRulesetContent: String? by lazy {
    val customTestRuleSetFile = pmdConfig.testRuleSetFile.orNull
    when {
        customTestRuleSetFile != null -> rootProject.file(customTestRuleSetFile)
            .takeIf { it.exists() }?.readText(Charsets.UTF_8)
        checkJarFile != null -> CheckArtifactUtils.readFromJar(checkJarFile!!, "pmdRuleset-test.xml")
        else -> null
    }
}

val generatedPmdFilterDir: Provider<Directory> =
    layout.buildDirectory.dir("classes/java/main/META-INF/pmd-filters")

val mergedMainRulesetFile: Provider<RegularFile> =
    layout.buildDirectory.file("pmd/merged-ruleset.xml")

val mergedTestRulesetFile: Provider<RegularFile> =
    layout.buildDirectory.file("pmd/merged-ruleset-test.xml")

val mergePmdFilter = tasks.register("mergePmdFilter") {
    group = Defaults.TaskGroup.VERIFICATION_OTHER
    description = "Merges the base PMD ruleset with annotation-processor-generated rule suppressions."
    dependsOn(tasks.named("compileJava"))

    val generatedDir: File = generatedPmdFilterDir.get().asFile
    val mainOut: File = mergedMainRulesetFile.get().asFile
    val testOut: File = mergedTestRulesetFile.get().asFile
    val mainBase: String? = resolvedBaseRulesetContent
    val testBase: String? = resolvedTestRulesetContent

    inputs.files(project.fileTree(generatedDir) { include("*.xml") })
        .withPropertyName("generatedPmdFilters")
        .optional()
    outputs.file(mainOut).withPropertyName("mergedMainRuleset")
    outputs.file(testOut).withPropertyName("mergedTestRuleset")

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

        mainOut.parentFile.mkdirs()
        if (mainBase != null) {
            mainOut.writeText(PmdRulesetMerger.merge(mainBase, fragments), Charsets.UTF_8)
        }
        if (testBase != null) {
            testOut.writeText(PmdRulesetMerger.merge(testBase, fragments), Charsets.UTF_8)
        }
    }
}

pmd {
    toolVersion = libs?.let { getVersionOrNull(it, "pmd") } ?: defaultPmdVersion
    ruleSets = emptyList()

    val customRuleSetFile = pmdConfig.ruleSetFile.orNull
    when {
        customRuleSetFile != null -> ruleSetFiles = files(mergedMainRulesetFile)
        checkJarFile != null -> ruleSetFiles = files(mergedMainRulesetFile)
        else -> ruleSets = listOf("category/java/bestpractices.xml")
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
    dependsOn(mergePmdFilter)

    if (checkJarFile != null) {
        pmdClasspath = pmdClasspath.plus(files(checkJarFile!!))
    }

    val useTestRuleset = name != "pmdMain" || pmdConfig.useTestRulesetForMain.get()
    if (useTestRuleset) {
        ruleSetFiles = files(mergedTestRulesetFile)
    }
}

tasks.withType<Jar>().configureEach {
    exclude("META-INF/pmd-filters/**")
}

project.configureAllCheckTasksWithJavaPlugin<Pmd>()
