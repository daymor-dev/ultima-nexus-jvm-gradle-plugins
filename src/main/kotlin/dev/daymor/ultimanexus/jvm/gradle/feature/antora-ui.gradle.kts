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

package dev.daymor.ultimanexus.jvm.gradle.feature

import com.github.gradle.node.npm.task.NpxTask

/**
 * Feature plugin for Antora UI bundle generation.
 * Provides tasks for building, previewing, and packaging
 * Antora UI bundles using Gulp.
 *
 * Usage:
 * plugins {
 *     id("dev.daymor.ultimanexus.jvm.gradle.feature.antora-ui")
 * }
 */
plugins { id("com.github.node-gradle.node") }

tasks.register<NpxTask>("antoraUIBundle") {
    group = "documentation"
    command.set("gulp")
    args.set(listOf("bundle"))
}

tasks.register<NpxTask>("antoraUIBundleKeepPreview") {
    group = "documentation"
    command.set("gulp")
    args.set(listOf("bundle:pack"))
}

tasks.register<NpxTask>("antoraUIPreview") {
    group = "documentation"
    command.set("gulp")
    args.set(listOf("preview:build"))
}
