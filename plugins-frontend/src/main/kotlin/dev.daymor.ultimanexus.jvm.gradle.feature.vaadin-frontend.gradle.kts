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

import com.vaadin.flow.gradle.VaadinFlowPluginExtension
import dev.daymor.ultimanexus.jvm.gradle.config.Defaults
import dev.daymor.ultimanexus.jvm.gradle.config.PropertyKeys
import dev.daymor.ultimanexus.jvm.gradle.util.PropertyUtils.findPropertyOrNull

/**
 * Plugin: dev.daymor.ultimanexus.jvm.gradle.feature.vaadin-frontend
 *
 * Applies the Vaadin Gradle plugin and takes care of the two build-time
 * details any Vaadin web application needs: a production frontend bundle, and
 * having that bundle built before the tests that start the application. It is a
 * self-contained convention plugin with no dependency on any particular
 * application framework or set of UI modules — it wires only the Vaadin tool,
 * so it works on a plain servlet app as well as a Spring app.
 *
 * What it does:
 *   - Applies `com.vaadin`, so the `vaadinPrepareFrontend` / `vaadinBuildFrontend`
 *     tasks and the frontend build are available.
 *   - Sets `vaadin.productionMode = true` by default, so the served servlet runs
 *     against the built production bundle. Override with
 *     `ultimaNexusJvm.frontend.vaadinProductionMode=false` (gradle.properties or
 *     -P) to run the app in Vaadin development mode locally.
 *   - Makes the `test`, `integrationTest`, and `functionalTest` tasks depend on
 *     `vaadinBuildFrontend`, so a full-context test exercises the real servlet
 *     against the built bundle rather than a missing one.
 *
 * This plugin adds no UI dependencies of its own — the consumer declares the
 * Vaadin views/components it needs. On a Spring Boot application, apply
 * `feature.spring-vaadin-frontend` on top of this plugin for the Vaadin Spring
 * Boot integration. Ultima Nexus JVM users who want the generated Vaadin
 * frontend modules wired in for zero-config apply the
 * `bundle.ultima-nexus-frontend-vaadin` bundle, which layers those modules on
 * top.
 *
 * Usage:
 * ```kotlin
 * plugins {
 *     id("dev.daymor.ultimanexus.jvm.gradle.feature.vaadin-frontend")
 * }
 * ```
 */

plugins {
    java
    id("com.vaadin")
}

val vaadinProductionMode = project.findPropertyOrNull(PropertyKeys.Frontend.VAADIN_PRODUCTION_MODE)
    ?.toBoolean() ?: Defaults.Frontend.DEFAULT_VAADIN_PRODUCTION_MODE

extensions.configure<VaadinFlowPluginExtension> { productionMode.set(vaadinProductionMode) }

val bootedTasks = buildList {
    addAll(listOf("test", "integrationTest", "functionalTest"))

    if (vaadinProductionMode) {

        add("bootRun")
    }
}

bootedTasks.forEach { bootedTask ->
    if (tasks.names.contains(bootedTask)) {
        tasks.named(bootedTask) { dependsOn("vaadinBuildFrontend") }
    }
}
