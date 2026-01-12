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


import dev.daymor.ultimanexus.jvm.gradle.config.PluginIds

/*
 * Complete settings bundle for Ultima Nexus JVM multi-module projects.
 *
 * This is the all-in-one plugin for multi-module projects. Apply it once
 * in settings.gradle.kts and get everything configured automatically:
 *
 * - Project structure discovery (auto-includes subprojects)
 * - Git pre-commit hooks
 * - Maven Central publishing aggregation
 * - Root project: lifecycle.root, dependencies.root, format-gradle.root, aggregation
 * - Platform projects (auto-detected): version-platform, identity
 * - All other subprojects: Full ultima-nexus bundle (Spring Boot + Lombok + all quality checks)
 *
 * Platform projects are detected by:
 * - Project name: platform, bom, versions, version-platform
 * - Property: projectType=platform in gradle.properties
 * - Build file content: java-platform, version-platform, or bundle.platform
 *
 * Usage in settings.gradle.kts:
 * ```kotlin
 * plugins {
 *     id("dev.daymor.ultimanexus.jvm.gradle.bundle.ultima-nexus-complete") version "latest.release"
 * }
 * ```
 *
 * That's it! No need to configure individual build.gradle.kts files.
 */
plugins {
    id("dev.daymor.ultimanexus.jvm.gradle.bundle.ultima-nexus-jvm")
}

val platformProjectNames = listOf("platform", "bom", "versions", "version-platform")

val platformPluginPatterns = listOf(
    "java-platform",
    "feature.version-platform",
    "feature.use-all-catalog-versions",
    "bundle.platform"
)

fun isPlatformProject(project: Project): Boolean {
    if (platformProjectNames.any { project.name.equals(it, ignoreCase = true) }) {
        return true
    }

    val propsFile = project.file("gradle.properties")
    if (propsFile.exists()) {
        val props = java.util.Properties().apply { propsFile.inputStream().use { load(it) } }
        if (props.getProperty("projectType")?.equals("platform", ignoreCase = true) == true) {
            return true
        }
    }

    val buildFile = project.file("build.gradle.kts")
    if (buildFile.exists()) {
        val content = buildFile.readText()
        if (platformPluginPatterns.any { content.contains(it) }) {
            return true
        }
    }

    return false
}

gradle.beforeProject {
    if (this == rootProject) {
        apply(plugin = PluginIds.Bundle.GRADLE_PROJECT_ROOT)
    } else if (isPlatformProject(this)) {
        apply(plugin = PluginIds.Bundle.PLATFORM)
    } else {
        apply(plugin = PluginIds.Bundle.ULTIMA_NEXUS)
    }
}
