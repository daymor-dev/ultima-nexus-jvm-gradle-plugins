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

package dev.daymor.ultimanexus.jvm.gradle.util

import dev.daymor.ultimanexus.jvm.gradle.config.Defaults
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin

object TaskConfigUtils {

    fun Project.addToQualityGates(taskName: String) {
        tasks.named("qualityCheck") { dependsOn(tasks.named(taskName)) }
        tasks.named("qualityGate") { dependsOn(tasks.named(taskName)) }
    }

    fun Project.configureCheckTaskWithJavaPlugin(
        taskName: String,
        taskGroup: String = Defaults.TaskGroup.VERIFICATION
    ) {
        plugins.withType(JavaPlugin::class.java) {
            tasks.named(taskName) { group = taskGroup }
            addToQualityGates(taskName)
        }
    }
}
