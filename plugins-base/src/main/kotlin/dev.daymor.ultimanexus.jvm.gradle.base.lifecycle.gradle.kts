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
 * Convention plugin for lifecycle tasks.
 *
 * Provides:
 * - qualityCheck: Runs all quality checks without tests
 * - qualityGate: Runs checks with auto-corrections
 * - internal configuration: For code-coverage and test aggregation
 *
 * Usage:
 * ```kotlin
 * plugins {
 *     alias(libs.plugins.ultimanexus.jvm.base.lifecycle)
 * }
 * ```
 */
plugins { base }

configurations.findByName("internal") ?: configurations.create("internal") {
    isCanBeConsumed = false
    isCanBeResolved = true
}

tasks {
    register("qualityCheck") {
        group = "build"
        description = "Runs checks (without executing tests)"
    }
    register("qualityGate") {
        group = "build"
        description = "Runs checks and autocorrects (without executing tests)"
    }
    check { dependsOn(tasks.named("qualityCheck")) }
}
