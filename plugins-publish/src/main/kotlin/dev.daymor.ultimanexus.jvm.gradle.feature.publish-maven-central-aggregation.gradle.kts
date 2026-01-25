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

import dev.daymor.ultimanexus.jvm.gradle.config.Defaults
import dev.daymor.ultimanexus.jvm.gradle.config.PropertyKeys
import dev.daymor.ultimanexus.jvm.gradle.util.PropertyUtils.findPropertyOrNull

/**
 * Build plugin for explicit Maven Central publishing aggregation.
 * Use this if you want to explicitly control which modules to publish.
 *
 * For automatic aggregation (recommended), use the settings plugin:
 *   id("dev.daymor.ultimanexus.jvm.gradle.feature.publish-maven-central") version "latest.release"
 *
 * Usage in root build.gradle.kts:
 * ```kotlin
 * plugins {
 *     alias(libs.plugins.ultimanexus.jvm.feature.publish.maven.central.aggregation)
 * }
 *
 * // Option 1: Configure in build file
 * dependencies {
 *     nmcpAggregation(project(":core"))
 *     nmcpAggregation(project(":api"))
 * }
 *
 * // Option 2: Configure in gradle.properties
 * // publishModules=:core,:api,:web
 * ```
 *
 * Credentials (gradle.properties):
 * ```properties
 * mavenCentralUsername=<your-token-username>
 * mavenCentralPassword=<your-token-password>
 * ```
 *
 * Or environment variables:
 * ```
 * MAVENCENTRALUSERNAME=<your-token-username>
 * MAVENCENTRALPASSWORD=<your-token-password>
 * ```
 *
 * Publish command:
 *   ./gradlew publishAggregationToCentralPortal
 */
plugins {
    id("com.gradleup.nmcp.aggregation")
}

repositories {
    mavenCentral()
}

nmcpAggregation {
    centralPortal {
        username =
            project.findPropertyOrNull(PropertyKeys.Publish.MAVEN_CENTRAL_USERNAME)
                ?: System.getenv("MAVENCENTRALUSERNAME")
                ?: ""
        password =
            project.findPropertyOrNull(PropertyKeys.Publish.MAVEN_CENTRAL_PASSWORD)
                ?: System.getenv("MAVENCENTRALPASSWORD")
                ?: ""
        publishingType = "AUTOMATIC"
    }
}

project.findPropertyOrNull(PropertyKeys.Publish.MODULES)?.let { modules ->
    modules
        .split(",")
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .forEach { modulePath ->
            dependencies.add("nmcpAggregation", project(modulePath))
        }
}

tasks.matching {
    it.name.startsWith("publishAllPublicationsTo") ||
        it.name == "publishAggregationToCentralSnapshots"
}.configureEach { group = Defaults.TaskGroup.PUBLISHING_OTHER }
