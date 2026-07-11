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
 * Plugin: dev.daymor.ultimanexus.jvm.gradle.feature.spring-htmx-frontend
 *
 * The Spring Boot layer on top of the framework-agnostic `feature.htmx-frontend`
 * plugin. It applies that plugin for the neutral htmx + Thymeleaf stack, then
 * adds the Spring integration: `spring-boot-starter-thymeleaf` (Thymeleaf
 * auto-configuration and the Spring view resolver) and `htmx-spring-boot` (the
 * htmx request / response integration for Spring MVC).
 *
 * Apply this on a Spring Boot application; a plain servlet app applies
 * `feature.htmx-frontend` directly and wires its own view resolution.
 *
 * Dependencies are resolved from the consumer's version catalog when present
 * (`spring-boot-starter-thymeleaf`, `htmx-spring-boot`), otherwise a pinned
 * fallback.
 *
 * Usage:
 * ```kotlin
 * plugins {
 *     id("dev.daymor.ultimanexus.jvm.gradle.feature.spring-htmx-frontend")
 * }
 * ```
 */

plugins {
    id("dev.daymor.ultimanexus.jvm.gradle.feature.htmx-frontend")
}

val libs = getLibsCatalogOrNull(project)

fun frontendDependency(catalogName: String, fallback: String): Any =
    libs?.let { getLibraryOrNull(it, catalogName) } ?: fallback

dependencies {
    add(
        DependencyScope.IMPLEMENTATION,
        frontendDependency(CatalogLibrary.SPRING_BOOT_STARTER_THYMELEAF, Fallbacks.SPRING_BOOT_STARTER_THYMELEAF)
    )
    add(
        DependencyScope.IMPLEMENTATION,
        frontendDependency(CatalogLibrary.HTMX_SPRING_BOOT, Fallbacks.HTMX_SPRING_BOOT)
    )
}
