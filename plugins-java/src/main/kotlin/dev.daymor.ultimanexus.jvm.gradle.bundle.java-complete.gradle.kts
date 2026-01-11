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


import dev.daymor.ultimanexus.jvm.gradle.config.PluginIds
import dev.daymor.ultimanexus.jvm.gradle.config.UltimaNexusConfig

/*
 * Plugin: dev.daymor.ultimanexus.jvm.gradle.bundle.java-complete
 *
 * Bundle plugin for complete Java development with application/library mode support.
 * Includes quality checks, testing, reporting, Javadoc, and JSpecify annotations.
 *
 * Publishing is conditionally included based on the application mode:
 * - Application mode (default): Does NOT include publish-java plugin
 * - Library mode: Includes publish-java plugin for Maven publishing
 *
 * Extension configuration:
 *     ultimaNexus {
 *         applicationMode.set(false)  // Library mode, enables publishing
 *     }
 *
 * Property configuration (gradle.properties):
 *     java.isApplication=false
 *
 * Default: Application mode (true)
 */
plugins {
    id("dev.daymor.ultimanexus.jvm.gradle.bundle.gradle-project")
    id("dev.daymor.ultimanexus.jvm.gradle.bundle.check")
    id("dev.daymor.ultimanexus.jvm.gradle.bundle.test")
    id("dev.daymor.ultimanexus.jvm.gradle.bundle.report")
    id("dev.daymor.ultimanexus.jvm.gradle.feature.compile-java")
    id("dev.daymor.ultimanexus.jvm.gradle.feature.java-unified")
    id("dev.daymor.ultimanexus.jvm.gradle.feature.javadoc")
    id("dev.daymor.ultimanexus.jvm.gradle.dependency.jspecify")
}

val config = UltimaNexusConfig.get(project)

afterEvaluate {
    val isLibrary = !config.applicationMode.get()
    if (isLibrary) {
        apply(plugin = PluginIds.Feature.PUBLISH_JAVA)
    }
}
