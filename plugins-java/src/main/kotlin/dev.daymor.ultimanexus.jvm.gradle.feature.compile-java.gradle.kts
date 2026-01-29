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


import dev.daymor.ultimanexus.jvm.gradle.config.Defaults
import dev.daymor.ultimanexus.jvm.gradle.config.PropertyKeys
import dev.daymor.ultimanexus.jvm.gradle.util.PropertyUtils.findPropertyAsInt

/**
 * Plugin: dev.daymor.ultimanexus.jvm.gradle.feature.compile-java
 *
 * Configures Java compilation settings including JDK version, compiler options,
 * and reproducible archive settings.
 *
 * Extension configuration:
 *     compileJavaConfig {
 *         jdkVersion.set(21)
 *     }
 *
 * Property configuration (gradle.properties):
 *     jdkVersion=21
 *
 * Default JDK version: 25
 */
plugins {
    java
    id("dev.daymor.ultimanexus.jvm.gradle.base.lifecycle")
}

interface CompileJavaConfigExtension {
    val jdkVersion: Property<Int>
}

val compileJavaConfig = extensions.create<CompileJavaConfigExtension>("compileJavaConfig")

compileJavaConfig.jdkVersion.convention(
    project.findPropertyAsInt(PropertyKeys.JDK_VERSION, Defaults.JDK_VERSION)
)

java {
    toolchain {
        languageVersion = compileJavaConfig.jdkVersion.map { JavaLanguageVersion.of(it) }
    }
}

tasks {
    withType<JavaCompile>().configureEach {
        options.apply {
            isFork = true
            encoding = Defaults.FILE_ENCODING
            compilerArgs.add("-parameters")
            compilerArgs.add("-implicit:none")
            compilerArgs.add("-Werror")
            compilerArgs.add("-Xlint:all,-serial,-processing")
        }
    }
    withType<AbstractArchiveTask>().configureEach {
        isPreserveFileTimestamps = false
        isReproducibleFileOrder = true
        filePermissions { unix("0664") }
        dirPermissions { unix("0775") }
    }
    named("qualityCheck") { dependsOn(tasks.withType<JavaCompile>()) }
    named("qualityGate") { dependsOn(tasks.withType<JavaCompile>()) }
    buildDependents { group = "other" }
    buildNeeded { group = "other" }
    jar { group = "other" }
}

sourceSets.all { tasks.named(classesTaskName) { group = null } }
