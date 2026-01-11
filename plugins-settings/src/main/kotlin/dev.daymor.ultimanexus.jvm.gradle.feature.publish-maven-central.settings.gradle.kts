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

import dev.daymor.ultimanexus.jvm.gradle.config.PropertyKeys
import nmcp.NmcpSettings

/**
 * Settings plugin for publishing multi-module projects to Maven Central.
 * Automatically aggregates all projects that apply the publish plugin.
 *
 * Usage in settings.gradle.kts:
 * ```kotlin
 * plugins {
 *     alias(libs.plugins.ultimanexus.jvm.feature.publish.maven.central)
 * }
 * ```
 *
 * Configuration (gradle.properties):
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
    id("com.gradleup.nmcp.settings")
}

// Get credentials from properties or environment
val mavenCentralUsername = providers.gradleProperty(PropertyKeys.Publish.MAVEN_CENTRAL_USERNAME).orNull
    ?: System.getenv("MAVENCENTRALUSERNAME")
    ?: ""
val mavenCentralPassword = providers.gradleProperty(PropertyKeys.Publish.MAVEN_CENTRAL_PASSWORD).orNull
    ?: System.getenv("MAVENCENTRALPASSWORD")
    ?: ""

extensions.configure<NmcpSettings>("nmcpSettings") {
    centralPortal {
        username = mavenCentralUsername
        password = mavenCentralPassword
        publishingType = "AUTOMATIC"
    }
}
