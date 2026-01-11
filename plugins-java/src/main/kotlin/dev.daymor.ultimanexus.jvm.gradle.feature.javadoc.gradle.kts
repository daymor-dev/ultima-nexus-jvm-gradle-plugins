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
 * Plugin: dev.daymor.ultimanexus.jvm.gradle.feature.javadoc
 *
 * Configures Javadoc generation with strict error checking and UTF-8 encoding.
 * Adds Javadoc task to the qualityCheck lifecycle task.
 */
plugins {
    java
    id("dev.daymor.ultimanexus.jvm.gradle.base.lifecycle")
}

tasks.withType<Javadoc>().configureEach {
    options {
        this as StandardJavadocDocletOptions
        encoding = "UTF-8"
        addStringOption("Xwerror", "-Xdoclint:all,-missing")
    }
}

tasks.named("qualityCheck") { dependsOn(tasks.withType<Javadoc>()) }
