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

package dev.daymor.ultimanexus.jvm.gradle.check

plugins { id("dev.daymor.ultimanexus.jvm.gradle.check.spotless-base") }

/**
 * Extension to configure the root Gradle formatting targets.
 */
interface FormatGradleRootExtension {
    /**
     * The target pattern for Kotlin Gradle files.
     * Defaults to "gradle/plugins/src/main/....gradle.kts".
     */
    val kotlinGradleTarget: Property<String>

    /**
     * The target pattern for Kotlin files.
     * Defaults to "gradle/plugins/src/main/....kt".
     */
    val kotlinTarget: Property<String>
}

val formatGradleRoot =
    extensions.create<FormatGradleRootExtension>("formatGradleRoot")

// Read from gradle.properties with defaults
val kotlinGradleTargetFromProps = providers.gradleProperty("formatGradleRoot.kotlinGradleTarget").orNull
val kotlinTargetFromProps = providers.gradleProperty("formatGradleRoot.kotlinTarget").orNull

// Set conventions from gradle.properties
if (kotlinGradleTargetFromProps != null) formatGradleRoot.kotlinGradleTarget.convention(kotlinGradleTargetFromProps)
if (kotlinTargetFromProps != null) formatGradleRoot.kotlinTarget.convention(kotlinTargetFromProps)

afterEvaluate {
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
}
