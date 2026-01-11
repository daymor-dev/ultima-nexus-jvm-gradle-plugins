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

import dev.daymor.ultimanexus.jvm.gradle.spotless.SortDependenciesStep

/**
 * Convention plugin for Gradle Kotlin DSL file formatting using Spotless with ktfmt.
 *
 * This plugin applies to *.gradle.kts files in subprojects. It uses kotlinlang style
 * with 80 character max width and includes dependency sorting.
 *
 * For root project Gradle file formatting, use the format-gradle.root plugin instead.
 */
plugins {
    id("dev.daymor.ultimanexus.jvm.gradle.check.spotless-base")
    id("dev.daymor.ultimanexus.jvm.gradle.base.repositories")
}

spotless.kotlinGradle {
    ktfmt().kotlinlangStyle().configure { it.setMaxWidth(80) }
    addStep(SortDependenciesStep.create())
}
