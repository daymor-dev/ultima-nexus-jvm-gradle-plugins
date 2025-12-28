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

plugins {
    id("com.gradleup.nmcp.aggregation")
}

/**
 * Aggregation plugin for publishing multi-module projects to Maven Central.
 *
 * Apply this plugin to the root project of a multi-module build.
 * Each submodule that should be published must apply the `publish` plugin.
 *
 * Example usage in root build.gradle.kts:
 * ```kotlin
 * plugins {
 *     id("dev.daymor.ultimanexus.jvm.gradle.feature.publish-maven-central")
 * }
 * ```
 *
 * Then publish using:
 * ```bash
 * ./gradlew publishAggregationToCentralPortal
 * ```
 */
nmcpAggregation {
    centralPortal {
        username =
            providers.gradleProperty("mavenCentralUsername").orNull
                ?: System.getenv("MAVENCENTRALUSERNAME")
                ?: ""
        password =
            providers.gradleProperty("mavenCentralPassword").orNull
                ?: System.getenv("MAVENCENTRALPASSWORD")
                ?: ""
        publishingType = "AUTOMATIC"
    }
    // Publish all projects that apply the 'maven-publish' plugin
    publishAllProjectsProbablyBreakingProjectIsolation()
}

tasks.matching { it.name.startsWith("publishAllPublicationsTo") }
    .configureEach { group = "publishing.other" }
