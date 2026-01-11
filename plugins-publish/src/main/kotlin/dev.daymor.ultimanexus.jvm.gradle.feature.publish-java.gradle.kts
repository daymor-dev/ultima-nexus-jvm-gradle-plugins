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

/*
 * Plugin: dev.daymor.ultimanexus.jvm.gradle.feature.publish-java
 *
 * Configures Java projects for publishing with sources and javadoc jars.
 * Applies the base publish plugin and adds standard Java publishing artifacts.
 *
 * Usage:
 *   plugins {
 *       id("dev.daymor.ultimanexus.jvm.gradle.feature.publish-java")
 *   }
 *
 * This plugin automatically:
 * - Applies the base publish plugin
 * - Generates sources JAR
 * - Generates javadoc JAR
 *
 * No additional configuration is required. For publishing metadata configuration,
 * see the publish plugin documentation.
 */

plugins { id("dev.daymor.ultimanexus.jvm.gradle.feature.publish") }

java {
    withSourcesJar()
    withJavadocJar()
}

tasks {
    named("sourcesJar") { group = null }
    named("javadocJar") { group = null }
}
