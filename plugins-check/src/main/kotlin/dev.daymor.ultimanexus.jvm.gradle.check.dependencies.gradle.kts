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
import io.fuchs.gradle.collisiondetector.DetectCollisionsTask

/**
 * Convention plugin for dependency analysis in subprojects.
 *
 * This plugin applies dependency-analysis-gradle-plugin and classpath-collision-detector
 * to analyze dependency health and detect classpath collisions.
 *
 * For root project configuration with severity settings, use the dependencies.root plugin.
 */
plugins {
    java
    id("com.autonomousapps.dependency-analysis")
    id("io.fuchs.gradle.classpath-collision-detector")
    id("dev.daymor.ultimanexus.jvm.gradle.base.lifecycle")
}

tasks.named<DetectCollisionsTask>("detectCollisions").configure {
    collisionFilter { exclude("**.html", "**.txt", "LICENSE") }
}

tasks {
    named("qualityCheck") {
        dependsOn(tasks.detectCollisions)
        dependsOn(tasks.withType<ProjectHealthTask>())
    }
    named("qualityGate") {
        dependsOn(tasks.detectCollisions)
        dependsOn(tasks.withType<ProjectHealthTask>())
    }
}
