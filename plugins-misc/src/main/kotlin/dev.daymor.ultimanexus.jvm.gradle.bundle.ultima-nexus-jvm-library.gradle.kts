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

/*
 * Plugin: dev.daymor.ultimanexus.jvm.gradle.bundle.ultima-nexus-jvm-library
 *
 * Bundle plugin for full Ultima Nexus framework library experience.
 * Includes all quality checks, testing, Spring Boot library setup,
 * jspecify nullability, lombok support, Maven publishing,
 * and documentation (auto-detected).
 *
 * For Spring Boot starters and shared libraries with the complete Ultima Nexus toolchain.
 *
 * Usage:
 * ```kotlin
 * plugins {
 *     alias(libs.plugins.ultimanexus.bundle.ultima.nexus.jvm.library)
 * }
 * ```
 */
plugins {
    id("dev.daymor.ultimanexus.jvm.gradle.bundle.spring-boot-complete-library")
    id("dev.daymor.ultimanexus.jvm.gradle.bundle.lombok")
}

if (file("antora-playbook.yml").exists()) {
    apply(plugin = PluginIds.Bundle.DOCUMENTATION)
}
