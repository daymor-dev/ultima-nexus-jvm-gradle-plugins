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

/**
 * Applies the framework-neutral MapStruct wiring and overlays the Spring component model.
 *
 * The plugin applies `feature.mapstruct` (so MapStruct, mapstruct-processor and the Lombok
 * binding are all on the right configurations) and additionally sets the annotation-processor
 * option `-Amapstruct.defaultComponentModel=spring` on every `JavaCompile` task. The result is
 * that every generated `*Impl` class is emitted with `@Component` and constructor-injected
 * dependencies, without the source `@Mapper` interface mentioning Spring at all.
 *
 * This pattern keeps the mapper interfaces themselves DI-framework-neutral (as required by the
 * framework's hexagonal layering rules), while letting downstream Spring-based applications get
 * automatic component-scanning of the generated impls.
 *
 * Usage:
 * ```kotlin
 * plugins {
 *     id("dev.daymor.ultimanexus.jvm.gradle.feature.mapstruct-spring")
 * }
 * ```
 *
 * Apply this plugin instead of `feature.mapstruct` — applying both is harmless but redundant.
 */

plugins {
    id("dev.daymor.ultimanexus.jvm.gradle.feature.mapstruct")
}

tasks.named<JavaCompile>("compileJava") {
    options.compilerArgs.add("-Amapstruct.defaultComponentModel=spring")
}
