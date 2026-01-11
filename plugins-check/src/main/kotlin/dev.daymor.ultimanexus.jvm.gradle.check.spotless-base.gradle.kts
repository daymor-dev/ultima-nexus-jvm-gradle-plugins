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

import com.diffplug.gradle.spotless.SpotlessApply
import com.diffplug.gradle.spotless.SpotlessCheck
import com.diffplug.gradle.spotless.SpotlessDiagnoseTask
import com.diffplug.gradle.spotless.SpotlessTask
import dev.daymor.ultimanexus.jvm.gradle.config.Defaults

/**
 * Base convention plugin for Spotless code formatting integration.
 *
 * This plugin provides the foundation for all Spotless-based formatting plugins.
 * It configures task groups and wires spotlessCheck/spotlessApply into the
 * qualityCheck and qualityGate lifecycle tasks.
 *
 * Do not apply this plugin directly. Instead, use one of the format plugins:
 * - format-java: Java source formatting with Eclipse formatter
 * - format-gradle: Gradle Kotlin DSL formatting with ktfmt
 * - format-gradle.root: Root project Gradle/Kotlin formatting with ktfmt
 */
plugins {
    id("com.diffplug.spotless")
    id("dev.daymor.ultimanexus.jvm.gradle.base.lifecycle")
}

listOf(
        SpotlessTask::class,
        SpotlessCheck::class,
        SpotlessApply::class,
        SpotlessDiagnoseTask::class,
    )
    .forEach { taskType ->
        tasks.withType(taskType.java) { group = Defaults.TaskGroup.VERIFICATION_OTHER }
    }

tasks.spotlessCheck { group = Defaults.TaskGroup.VERIFICATION }

tasks.spotlessApply { group = Defaults.TaskGroup.BUILD }

tasks {
    named("qualityCheck") { dependsOn(tasks.spotlessCheck) }
    named("qualityGate") { dependsOn(tasks.spotlessApply) }
}
