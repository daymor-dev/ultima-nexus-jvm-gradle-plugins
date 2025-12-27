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

package dev.daymor.ultimanexus.jvm.gradle.base

plugins { base }

/**
 * Extension to configure project identity (group ID, version, and description).
 * Users must set the group ID for their projects.
 * Version and description are optional and can be set via this extension or gradle.properties.
 */
interface IdentityExtension {

    /**
     * The group ID for the project.
     * This is required and must be set by the user.
     */
    val groupId: Property<String>

    /**
     * The version for the project.
     * This is optional and can be set via this extension or gradle.properties.
     */
    val version: Property<String>

    /**
     * The description for the project.
     * This is optional and can be set via this extension or gradle.properties.
     */
    val description: Property<String>
}

val identity = extensions.create<IdentityExtension>("identity")

// Get groupId, version, and description from gradle.properties
val groupIdFromProperties = providers.gradleProperty("groupId").orNull
val versionFromProperties = providers.gradleProperty("version").orNull
val descriptionFromProperties = providers.gradleProperty("description").orNull

afterEvaluate {
    val configuredGroupId = identity.groupId.orNull
        ?: groupIdFromProperties

    require(configuredGroupId != null) {
        """
        |Project group ID is not configured.
        |Please configure it in one of these ways:
        |
        |1. In build.gradle.kts:
        |   identity {
        |       groupId = "com.example.yourproject"
        |   }
        |
        |2. In gradle.properties:
        |   groupId=com.example.yourproject
        """.trimMargin()
    }
    group = configuredGroupId

    // Set version if configured (optional - unlike groupId)
    val configuredVersion = identity.version.orNull ?: versionFromProperties
    if (configuredVersion != null) {
        version = configuredVersion
    }

    // Set description if configured (optional)
    val configuredDescription = identity.description.orNull ?: descriptionFromProperties
    if (configuredDescription != null) {
        description = configuredDescription
    }
}
