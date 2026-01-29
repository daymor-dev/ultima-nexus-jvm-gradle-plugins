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
 * Plugin: dev.daymor.ultimanexus.jvm.gradle.bundle.ultima-nexus-jvm-application
 *
 * Bundle plugin for full Ultima Nexus framework application experience.
 * Includes all quality checks, testing, Spring Boot application setup,
 * jspecify nullability, lombok support, and documentation (auto-detected).
 *
 * For standalone Spring Boot applications with the complete Ultima Nexus toolchain.
 *
 * Usage:
 * ```kotlin
 * plugins {
 *     alias(libs.plugins.ultimanexus.bundle.ultima.nexus.jvm.application)
 * }
 * ```
 */
plugins {
    id("dev.daymor.ultimanexus.jvm.gradle.bundle.spring-boot-complete-application")
    id("dev.daymor.ultimanexus.jvm.gradle.bundle.lombok")
}

if (file("antora-playbook.yml").exists()) {
    apply(plugin = PluginIds.Bundle.DOCUMENTATION)
}
