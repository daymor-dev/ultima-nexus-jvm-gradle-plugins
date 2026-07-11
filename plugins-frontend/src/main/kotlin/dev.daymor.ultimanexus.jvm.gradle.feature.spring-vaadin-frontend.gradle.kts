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

import dev.daymor.ultimanexus.jvm.gradle.config.Defaults.DependencyScope
import dev.daymor.ultimanexus.jvm.gradle.config.Defaults.UltimaNexusJvm.CatalogLibrary
import dev.daymor.ultimanexus.jvm.gradle.util.DependencyUtils.Fallbacks
import dev.daymor.ultimanexus.jvm.gradle.util.DependencyUtils.getLibraryOrNull
import dev.daymor.ultimanexus.jvm.gradle.util.DependencyUtils.getLibsCatalogOrNull

/**
 * Plugin: dev.daymor.ultimanexus.jvm.gradle.feature.spring-vaadin-frontend
 *
 * The Spring Boot layer on top of the framework-agnostic
 * `feature.vaadin-frontend` plugin. It applies that plugin for the Vaadin build
 * wiring (production bundle, test-task ordering), then adds
 * `vaadin-spring-boot-starter` — the Vaadin Spring Boot integration that
 * auto-configures the Vaadin servlet and Spring-managed route/component
 * instantiation.
 *
 * Apply this on a Spring Boot application; a plain servlet app applies
 * `feature.vaadin-frontend` directly and registers the Vaadin servlet itself.
 *
 * The dependency is resolved from the consumer's version catalog when present
 * (`vaadin-spring-boot-starter`), otherwise a pinned fallback.
 *
 * Usage:
 * ```kotlin
 * plugins {
 *     id("dev.daymor.ultimanexus.jvm.gradle.feature.spring-vaadin-frontend")
 * }
 * ```
 */

plugins {
    id("dev.daymor.ultimanexus.jvm.gradle.feature.vaadin-frontend")
}

val libs = getLibsCatalogOrNull(project)

fun frontendDependency(catalogName: String, fallback: String): Any =
    libs?.let { getLibraryOrNull(it, catalogName) } ?: fallback

dependencies {
    add(
        DependencyScope.IMPLEMENTATION,
        frontendDependency(CatalogLibrary.VAADIN_SPRING_BOOT_STARTER, Fallbacks.VAADIN_SPRING_BOOT_STARTER)
    )
}
