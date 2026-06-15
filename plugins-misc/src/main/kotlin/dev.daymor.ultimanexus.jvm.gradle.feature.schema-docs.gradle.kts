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

/**
 * Feature plugin that collects the schema documentation emitted by the
 * Ultima Nexus annotation processor — Mermaid ER diagrams (`.mmd`) and
 * DBML (`.dbml`) — into a single output directory.
 *
 * The processor always writes these files to
 * `build/classes/java/main/META-INF/schema-docs/` during compilation;
 * this plugin adds an opt-in `generateSchemaDocs` task that copies them
 * to `build/schema-docs/` for embedding in Antora pages or uploading to
 * dbdocs.io. The task is not wired into `build` or `check` — run it
 * explicitly with `./gradlew generateSchemaDocs`.
 *
 * Usage:
 * ```kotlin
 * plugins {
 *     id("dev.daymor.ultimanexus.jvm.gradle.feature.schema-docs")
 * }
 * ```
 */

val generatedSchemaDocsDir: Provider<Directory> =
    layout.buildDirectory.dir("classes/java/main/META-INF/schema-docs")

val collectedSchemaDocsDir: Provider<Directory> = layout.buildDirectory.dir("schema-docs")

tasks.register<Copy>("generateSchemaDocs") {
    group = Defaults.TaskGroup.DOCUMENTATION
    description = "Collects the generated Mermaid (.mmd) and DBML (.dbml) schema documentation."
    dependsOn(tasks.named("compileJava"))

    from(generatedSchemaDocsDir) { include("*.mmd", "*.dbml") }
    into(collectedSchemaDocsDir)
}
