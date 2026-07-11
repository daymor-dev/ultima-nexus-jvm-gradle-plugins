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

import com.autonomousapps.tasks.ProjectHealthTask
import dev.daymor.ultimanexus.jvm.gradle.task.DetectClasspathCollisionsTask

/**
 * Convention plugin for dependency analysis in subprojects.
 *
 * This plugin applies dependency-analysis-gradle-plugin for dependency
 * health and registers an in-house, configuration-cache-compatible
 * classpath-collision check. The check captures the runtime classpath
 * as a task input and scans the jar bytes directly — it parses no class
 * or Kotlin metadata, so it stays compatible as dependencies adopt newer
 * metadata formats.
 *
 * For root project configuration with severity settings, use the dependencies.root plugin.
 */
plugins {
    java
    id("com.autonomousapps.dependency-analysis")
    id("dev.daymor.ultimanexus.jvm.gradle.base.lifecycle")
}

val detectClasspathCollisions =
    tasks.register<DetectClasspathCollisionsTask>("detectClasspathCollisions") {
        group = "verification"
        description = "Fails the build when two jars provide the same class with differing bytes."
        classpath.from(configurations.named("runtimeClasspath"))
        resourceExclusions.convention(emptyList())
    }

tasks {
    named("qualityCheck") {
        dependsOn(detectClasspathCollisions)
        dependsOn(tasks.withType<ProjectHealthTask>())
    }
    named("qualityGate") {
        dependsOn(detectClasspathCollisions)
        dependsOn(tasks.withType<ProjectHealthTask>())
    }
}
