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

import dev.daymor.ultimanexus.jvm.gradle.config.PluginIds

/**
 * Bundle plugin for complete JVM experience (language-agnostic).
 * Includes quality checks, testing, reporting, and lombok support.
 * Does NOT include Java-specific features - add those separately if needed.
 *
 * For Java projects, use bundle.java-complete instead.
 * For Spring Boot projects, use bundle.ultima-nexus instead.
 *
 * Included plugins:
 *   - dev.daymor.ultimanexus.jvm.gradle.bundle.gradle-project
 *   - dev.daymor.ultimanexus.jvm.gradle.bundle.check
 *   - dev.daymor.ultimanexus.jvm.gradle.bundle.test
 *   - dev.daymor.ultimanexus.jvm.gradle.bundle.report
 *   - dev.daymor.ultimanexus.jvm.gradle.bundle.lombok
 *   - dev.daymor.ultimanexus.jvm.gradle.bundle.documentation (if antora-playbook.yml exists)
 *
 * Usage:
 * ```kotlin
 * plugins {
 *     alias(libs.plugins.ultimanexus.jvm.bundle.jvm.complete)
 *     // Add your language-specific plugins
 *     kotlin("jvm")  // for Kotlin
 * }
 * ```
 */
plugins {
    id("dev.daymor.ultimanexus.jvm.gradle.bundle.gradle-project")
    id("dev.daymor.ultimanexus.jvm.gradle.bundle.check")
    id("dev.daymor.ultimanexus.jvm.gradle.bundle.test")
    id("dev.daymor.ultimanexus.jvm.gradle.bundle.report")
    id("dev.daymor.ultimanexus.jvm.gradle.bundle.lombok")
}

if (file("antora-playbook.yml").exists()) {
    apply(plugin = PluginIds.Bundle.DOCUMENTATION)
}
