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
 * Plugin: dev.daymor.ultimanexus.jvm.gradle.bundle.ultima-nexus-frontend-htmx
 *
 * The Ultima Nexus JVM convenience layer for the htmx frontend. It applies
 * `feature.spring-htmx-frontend` (which itself applies the neutral
 * `feature.htmx-frontend`), so the neutral htmx + Thymeleaf stack and its Spring
 * integration are both present, then contributes the framework's
 * generated-frontend modules so a `@GenerateFrontend(backends = "htmx")`
 * entity's controller and shared fragments compile and run with no hand-written
 * Gradle.
 *
 * Adds (resolved from the consumer's version catalog when present, otherwise a
 * pinned fallback):
 *   - `frontend-htmx`, `configuration-spring-frontend`, and
 *     `application-contract-ui` — the framework's neutral UI model and its
 *     htmx rendering backend.
 *
 * A project that is not built on Ultima Nexus JVM applies
 * `feature.htmx-frontend` (plain servlet) or `feature.spring-htmx-frontend`
 * (Spring Boot) directly and writes its own Thymeleaf templates instead of this
 * bundle.
 *
 * Usage:
 * ```kotlin
 * plugins {
 *     id("dev.daymor.ultimanexus.jvm.gradle.bundle.ultima-nexus-frontend-htmx")
 * }
 * ```
 */

plugins {
    id("dev.daymor.ultimanexus.jvm.gradle.feature.spring-htmx-frontend")
}

val libs = getLibsCatalogOrNull(project)

fun frontendDependency(catalogName: String, fallback: String): Any =
    libs?.let { getLibraryOrNull(it, catalogName) } ?: fallback

dependencies {
    add(
        DependencyScope.IMPLEMENTATION,
        frontendDependency(CatalogLibrary.FRONTEND_HTMX, Fallbacks.ULTIMA_NEXUS_JVM_FRONTEND_HTMX)
    )
    add(
        DependencyScope.IMPLEMENTATION,
        frontendDependency(CatalogLibrary.CONFIGURATION_FRONTEND, Fallbacks.ULTIMA_NEXUS_JVM_CONFIGURATION_SPRING_FRONTEND)
    )
    add(
        DependencyScope.IMPLEMENTATION,
        frontendDependency(CatalogLibrary.CONTRACT_UI, Fallbacks.ULTIMA_NEXUS_JVM_APPLICATION_CONTRACT_UI)
    )
}
