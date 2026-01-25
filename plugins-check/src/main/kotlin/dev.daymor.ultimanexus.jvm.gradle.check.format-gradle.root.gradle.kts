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
import dev.daymor.ultimanexus.jvm.gradle.util.PropertyUtils.findPropertyOrNull

/**
 * Convention plugin for root project Gradle and Kotlin file formatting using Spotless with ktfmt.
 *
 * Configuration via formatGradleRoot extension:
 * ```kotlin
 * formatGradleRoot {
 *     kotlinGradleTarget = "gradle/plugins/src/main/**/*.gradle.kts"
 *     kotlinTarget = "gradle/plugins/src/main/**/*.kt"
 * }
 * ```
 *
 * Or via gradle.properties:
 * ```properties
 * format.gradle.kotlinGradleTarget = gradle/plugins/src/main/**/*.gradle.kts
 * format.gradle.kotlinTarget = gradle/plugins/src/main/**/*.kt
 * ```
 */
plugins { id("dev.daymor.ultimanexus.jvm.gradle.check.spotless-base") }

interface FormatGradleRootExtension {
    val kotlinGradleTarget: Property<String>
    val kotlinTarget: Property<String>
}

val formatGradleRoot =
    extensions.create<FormatGradleRootExtension>("formatGradleRoot")

val kotlinGradleTargetFromProps = project.findPropertyOrNull(PropertyKeys.Format.GRADLE_KOTLIN_TARGET)
val kotlinTargetFromProps = project.findPropertyOrNull(PropertyKeys.Format.GRADLE_KOTLIN_VERSION)

if (kotlinGradleTargetFromProps != null) formatGradleRoot.kotlinGradleTarget.convention(kotlinGradleTargetFromProps)
if (kotlinTargetFromProps != null) formatGradleRoot.kotlinTarget.convention(kotlinTargetFromProps)

spotless {
    kotlinGradle {
        ktfmt().kotlinlangStyle().configure { it.setMaxWidth(80) }
        target(
            formatGradleRoot.kotlinGradleTarget.getOrElse(
                "gradle/plugins/src/main/**/*.gradle.kts"
            )
        )
    }
    kotlin {
        ktfmt().kotlinlangStyle().configure { it.setMaxWidth(80) }
        target(
            formatGradleRoot.kotlinTarget.getOrElse(
                "gradle/plugins/src/main/**/*.kt"
            )
        )
    }
}
