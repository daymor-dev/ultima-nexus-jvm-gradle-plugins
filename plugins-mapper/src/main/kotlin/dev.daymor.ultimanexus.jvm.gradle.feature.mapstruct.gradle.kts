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
 * Wires the MapStruct annotation processor into the consuming project,
 * framework-neutral (no DI-framework integration).
 *
 * The plugin adds:
 *   - `org.mapstruct:mapstruct` to `implementation` (runtime + compile types: @Mapper, @MappingTarget, ...)
 *   - `org.mapstruct:mapstruct-processor` to `annotationProcessor`
 *   - `org.projectlombok:lombok-mapstruct-binding` to `annotationProcessor` IF Lombok is on the
 *     annotation-processor classpath (required since Lombok 1.18.16 for MapStruct to see Lombok-
 *     generated accessors)
 *
 * Generated `*Impl` classes use MapStruct's `default` component model — no Spring/CDI/JSR-330
 * annotations. They expose a public no-arg constructor AND a static `INSTANCE` accessor via
 * `Mappers.getMapper(...)`, so any caller can use either pattern. Consumers running under Spring
 * should apply the sibling `feature.mapstruct-spring` plugin, which adds the AP option
 * `-Amapstruct.defaultComponentModel=spring` to flip the entire compile.
 *
 * Versions are resolved from the consuming project's version catalog (libs.versions.toml) using
 * the `mapstruct`, `mapstruct-processor`, and `lombok-mapstruct-binding` aliases. Each falls back
 * to a pinned version (see DependencyUtils.Fallbacks) when the alias is missing.
 *
 * Usage:
 * ```kotlin
 * plugins {
 *     id("dev.daymor.ultimanexus.jvm.gradle.feature.mapstruct")
 * }
 * ```
 */

plugins {
    java
    id("dev.daymor.ultimanexus.jvm.gradle.base.dependency-rules")
}

val libs: VersionCatalog? = getLibsCatalogOrNull(project)

dependencies {
    val mapstructDep = libs?.let { getLibraryOrNull(it, "mapstruct") } ?: Fallbacks.MAPSTRUCT
    val mapstructProcessorDep =
        libs?.let { getLibraryOrNull(it, "mapstruct-processor") } ?: Fallbacks.MAPSTRUCT_PROCESSOR
    val lombokMapstructBindingDep =
        libs?.let { getLibraryOrNull(it, "lombok-mapstruct-binding") } ?: Fallbacks.LOMBOK_MAPSTRUCT_BINDING

    implementation(mapstructDep)
    annotationProcessor(mapstructProcessorDep)
    annotationProcessor(lombokMapstructBindingDep)
}
