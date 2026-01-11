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

/*
 * Plugin: dev.daymor.ultimanexus.jvm.gradle.feature.java-unified
 *
 * Unified Java plugin with configurable application/library mode.
 * Application mode applies the 'application' plugin for runnable projects.
 * Library mode applies the 'java-library' plugin for dependency consumption.
 *
 * Extension configuration:
 *     ultimaNexus {
 *         applicationMode.set(false)
 *     }
 *
 * Property configuration (gradle.properties):
 *     java.isApplication=false
 *
 * Default: Application mode (true)
 */
plugins {
    java
    id("dev.daymor.ultimanexus.jvm.gradle.base.dependency-rules")
}

val config = UltimaNexusConfig.get(project)

afterEvaluate {
    val isApp = config.applicationMode.get()

    if (isApp) {
        plugins.apply("application")
    } else {
        plugins.apply("java-library")
    }
}
