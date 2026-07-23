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


import dev.daymor.ultimanexus.jvm.gradle.util.DependencyUtils.Fallbacks
import dev.daymor.ultimanexus.jvm.gradle.util.DependencyUtils.getLibraryOrNull
import dev.daymor.ultimanexus.jvm.gradle.util.DependencyUtils.getLibsCatalogOrNull

/**
 * Wires DSL-JSON's compile-time converter generation into the consuming
 * project, so every `@CompiledJson` type is serialised through a generated
 * converter instead of runtime reflection.
 *
 * The plugin adds:
 *   - `com.dslplatform:dsl-json` to `implementation` (the runtime, and — since 2.x —
 *     the annotation processor live in the same artifact)
 *   - `com.dslplatform:dsl-json` to `annotationProcessor` (the processor that emits
 *     one `_<Type>_DslJsonConverter` per annotated type)
 *   - `-Adsljson.generatedmarker=...` to `compileJava`, so the generated converters
 *     carry `@SuppressWarnings({"rawtypes","unchecked"})`
 *
 * The generated marker is why this plugin exists rather than two catalog lines. DSL-JSON emits
 * its converters with raw-type and unchecked calls into its own API; a project compiling with
 * `-Werror` (as the framework's `feature.compile-java` does) would reject them. The processor's
 * `generatedmarker` option is emitted verbatim above each generated class, so carrying a
 * `@SuppressWarnings` there scopes the exemption to the vendor's own machine-written class and
 * leaves `-Werror` fully enforced on hand-written code compiled by the same task.
 *
 * The `dsl-json` version resolves from the consuming project's version catalog
 * (`libs.versions.toml`) via the `dsl-json` alias, falling back to a pinned version
 * (see DependencyUtils.Fallbacks) when the alias is missing. The plugin is
 * framework-agnostic — it is usable on any project that wants reflection-free DSL-JSON.
 *
 * Usage:
 * ```kotlin
 * plugins {
 *     id("dev.daymor.ultimanexus.jvm.gradle.feature.dsljson-codegen")
 * }
 * ```
 */

plugins {
    java
    id("dev.daymor.ultimanexus.jvm.gradle.base.dependency-rules")
}

val libs: VersionCatalog? = getLibsCatalogOrNull(project)

val dslJsonDep = libs?.let { getLibraryOrNull(it, "dsl-json") } ?: Fallbacks.DSL_JSON

dependencies {
    implementation(dslJsonDep)
    annotationProcessor(dslJsonDep)
}

tasks.named<JavaCompile>("compileJava") {
    options.compilerArgs.add(
        "-Adsljson.generatedmarker=@javax.annotation.processing.Generated(\"dsl_json\") " +
            "@SuppressWarnings({\"rawtypes\",\"unchecked\"})"
    )
}
