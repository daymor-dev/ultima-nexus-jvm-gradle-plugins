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

import dev.daymor.ultimanexus.jvm.gradle.config.UltimaNexusConfig

/**
 * Convention plugin for project identity configuration.
 *
 * Configuration via ultimaNexus extension:
 * ```kotlin
 * ultimaNexus {
 *     groupId = "com.example.yourproject"
 *     version = "latest.release"
 *     description = "My project description"
 * }
 * ```
 *
 * Or via gradle.properties:
 * ```properties
 * groupId = com.example.yourproject
 * version = latest.release
 * description = My project description
 * ```
 */
plugins { base }

val ultimaNexus = UltimaNexusConfig.get(project)

afterEvaluate {
    val configuredGroupId = ultimaNexus.groupId.orNull

    require(configuredGroupId != null) {
        """
        |Project group ID is not configured.
        |Please configure it in one of these ways:
        |
        |1. In build.gradle.kts:
        |   ultimaNexus {
        |       groupId = "com.example.yourproject"
        |   }
        |
        |2. In gradle.properties:
        |   groupId=com.example.yourproject
        """.trimMargin()
    }
    group = configuredGroupId

    ultimaNexus.version.orNull?.let { version = it }
    ultimaNexus.description.orNull?.let { description = it }
}
