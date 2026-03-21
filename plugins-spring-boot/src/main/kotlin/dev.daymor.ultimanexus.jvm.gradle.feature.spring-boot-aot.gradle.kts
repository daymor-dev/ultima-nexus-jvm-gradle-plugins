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

import dev.daymor.ultimanexus.jvm.gradle.config.PropertyKeys
import dev.daymor.ultimanexus.jvm.gradle.util.PropertyUtils.conventionFromProperty

/**
 * Plugin: dev.daymor.ultimanexus.jvm.gradle.feature.spring-boot-aot
 *
 * Feature plugin for Spring Boot AOT (Ahead-of-Time) compilation support.
 * Configures AOT-specific compiler settings to suppress warnings generated
 * by Spring Boot AOT-generated code.
 *
 * Spring AOT generates code that triggers -Xlint:unchecked and -Xlint:rawtypes
 * warnings. When combined with the compile-java plugin (which sets -Werror),
 * these warnings become build-breaking errors. This plugin suppresses them
 * on AOT compilation tasks only.
 *
 * Features:
 * - Suppresses unchecked and rawtypes warnings on compileAotJava task
 * - Suppresses unchecked and rawtypes warnings on compileAotTestJava task
 * - Excludes AOT source sets from quality checks (Checkstyle, PMD, SpotBugs)
 * - Resolves slf4j-impl capability conflicts in AOT classpaths
 * - Disables AOT processing when no main source exists
 * - Configurable via extension DSL or gradle.properties
 *
 * Extension configuration:
 * ```kotlin
 * springBootAotConfig {
 *     suppressWarnings.set(true)
 * }
 * ```
 *
 * Or via gradle.properties:
 * ```properties
 * springBootAot.suppressWarnings=true
 * ```
 */
plugins {
    java
}

interface SpringBootAotConfigExtension {
    val suppressWarnings: Property<Boolean>
}

val springBootAotConfig =
    extensions.create<SpringBootAotConfigExtension>("springBootAotConfig")

springBootAotConfig.suppressWarnings.conventionFromProperty(
    project, PropertyKeys.SpringBootAot.SUPPRESS_AOT_WARNINGS, true
)

tasks.withType<JavaCompile>().configureEach {
    if (name == "compileAotJava" || name == "compileAotTestJava") {
        options.compilerArgs.addAll(
            springBootAotConfig.suppressWarnings.map { suppress ->
                if (suppress) listOf("-Xlint:-unchecked", "-Xlint:-rawtypes")
                else emptyList()
            }.get()
        )
    }
}

configurations.configureEach {
    if (name.contains("Aot", ignoreCase = true)) {
        resolutionStrategy.capabilitiesResolution
            .withCapability("org.gradlex:slf4j-impl") {
                select("ch.qos.logback:logback-classic:0")
            }
    }
}

tasks.matching { it.name == "processAot" || it.name == "processTestAot" }
    .configureEach {
        enabled = project.file("src/main/java").exists()
    }

val aotQualityTasks = setOf(
    "checkstyleAot", "checkstyleAotTest",
    "pmdAot", "pmdAotTest",
    "spotbugsAot", "spotbugsAotTest",
)
tasks.matching { it.name in aotQualityTasks }.configureEach {
    enabled = false
}

