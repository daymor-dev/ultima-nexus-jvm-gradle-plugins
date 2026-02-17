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
 * Bundle plugin for JSpecify nullability annotations.
 * Applies JSpecify for both main and test source sets.
 *
 * Includes:
 * - dev.daymor.ultimanexus.jvm.gradle.dependency.jspecify
 * - dev.daymor.ultimanexus.jvm.gradle.dependency.jspecify-test
 *
 * The plugin resolves the JSpecify version from the version catalog (libs.versions.toml)
 * using the "jspecify" alias, with a fallback version if not defined.
 *
 * Version catalog configuration (optional):
 * ```toml
 * [libraries]
 * jspecify = { module = "org.jspecify:jspecify", version = "1.0.0" }
 * ```
 *
 * Usage:
 * ```kotlin
 * plugins {
 *     alias(libs.plugins.ultimanexus.jvm.bundle.jspecify)
 * }
 * ```
 */

import dev.daymor.ultimanexus.jvm.gradle.config.PluginIds

plugins {
    id("dev.daymor.ultimanexus.jvm.gradle.dependency.jspecify")
    id("dev.daymor.ultimanexus.jvm.gradle.dependency.jspecify-test")
}
