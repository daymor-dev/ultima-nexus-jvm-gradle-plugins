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
import dev.daymor.ultimanexus.jvm.gradle.util.PropertyUtils.conventionFromProperty

/**
 * Feature plugin for Antora documentation generation.
 * Configures Antora with Lunr search extension and sets up
 * documentation-related tasks.
 *
 * Configuration via antoraConfig extension:
 * ```kotlin
 * antoraConfig {
 *     staticFilePatterns = listOf("*.html", "*.png", "*.ico", "*.svg", "*.jpg", "*.webp")
 * }
 * ```
 *
 * Or via gradle.properties:
 * ```properties
 * antora.staticFilePatterns = *.html, *.png, *.ico, *.svg, *.jpg, *.webp
 * ```
 */
plugins {
    id("org.antora")
    id("com.github.node-gradle.node")
}

interface AntoraConfigExtension {
    val staticFilePatterns: ListProperty<String>
}

val antoraConfig = extensions.create<AntoraConfigExtension>("antoraConfig")

antoraConfig.staticFilePatterns.conventionFromProperty(project, PropertyKeys.Antora.STATIC_FILE_PATTERNS, Defaults.Antora.STATIC_FILE_PATTERNS)

antora { packages.put("@antora/lunr-extension", "latest") }

tasks.named("antora") {
    group = Defaults.TaskGroup.DOCUMENTATION
    finalizedBy("addStaticIndex")
}

tasks.register<Copy>("addStaticIndex") {
    from(fileTree(projectDir).matching { include(antoraConfig.staticFilePatterns.get()) })
    into(layout.buildDirectory)
}
