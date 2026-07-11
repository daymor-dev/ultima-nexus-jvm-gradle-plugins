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
 * Plugin: dev.daymor.ultimanexus.jvm.gradle.feature.htmx-frontend
 *
 * Adds the server-rendered htmx + Thymeleaf stack for any JVM web application:
 * the plain Thymeleaf template engine and the htmx JavaScript library (as a
 * WebJar). htmx is server-rendered and needs no build-time frontend bundle, so
 * this is a dependency-only convention plugin with no extra tasks and no
 * dependency on any particular application framework — it works on a plain
 * servlet app, a Spring app, or anything in between.
 *
 * The consumer writes the Thymeleaf templates and request handlers it needs.
 * On a Spring Boot application, apply `feature.spring-htmx-frontend` on top of
 * this plugin for the Spring auto-configuration and the htmx-Spring request /
 * response integration. Ultima Nexus JVM users who want the generated htmx
 * frontend modules wired in for zero-config apply the
 * `bundle.ultima-nexus-frontend-htmx` bundle, which layers those modules on top.
 *
 * Dependencies are resolved from the consumer's version catalog when present
 * (`thymeleaf`, `htmx-webjar`), otherwise a pinned fallback.
 *
 * Usage:
 * ```kotlin
 * plugins {
 *     id("dev.daymor.ultimanexus.jvm.gradle.feature.htmx-frontend")
 * }
 * ```
 */

plugins {
    java
}

val libs = getLibsCatalogOrNull(project)

fun frontendDependency(catalogName: String, fallback: String): Any =
    libs?.let { getLibraryOrNull(it, catalogName) } ?: fallback

dependencies {
    add(
        DependencyScope.IMPLEMENTATION,
        frontendDependency(CatalogLibrary.THYMELEAF, Fallbacks.THYMELEAF)
    )
    add(
        DependencyScope.IMPLEMENTATION,
        frontendDependency(CatalogLibrary.HTMX_WEBJAR, Fallbacks.HTMX_WEBJAR)
    )
}
