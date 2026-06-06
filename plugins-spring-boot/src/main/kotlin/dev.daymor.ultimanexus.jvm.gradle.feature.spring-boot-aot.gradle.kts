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

/**
 * Plugin: dev.daymor.ultimanexus.jvm.gradle.feature.spring-boot-aot
 *
 * Feature plugin for Spring Boot AOT (Ahead-of-Time) compilation support.
 *
 * The compile-java plugin already exempts the AOT compile tasks from -Werror
 * (Spring AOT-generated sources are machine-written), so this plugin only
 * handles the AOT classpath, lifecycle, and quality-check concerns.
 *
 * Features:
 * - Excludes AOT source sets from quality checks (Checkstyle, PMD, SpotBugs)
 * - Resolves slf4j-impl capability conflicts in AOT classpaths
 * - Disables AOT processing when no main source exists
 * - Skips processTestAot when spring-boot-test is absent from the test
 *   classpath (a pure unit-test source set has no AOT-processable tests)
 */
plugins {
    java
}

configurations.configureEach {
    if (name.contains("Aot", ignoreCase = true)) {
        resolutionStrategy.capabilitiesResolution
            .withCapability("org.gradlex:slf4j-impl") {
                select("ch.qos.logback:logback-classic:0")
            }
    }
}

val hasMainSource = project.file("src/main/java").exists()

tasks.matching { it.name == "processAot" }.configureEach {
    enabled = hasMainSource
}

tasks.matching { it.name == "processTestAot" }.configureEach {
    enabled = hasMainSource
    if (this is JavaExec) {
        val taskClasspath = classpath
        onlyIf {
            taskClasspath.any { it.name.startsWith("spring-boot-test-") }
        }
    }
}

val aotQualityTasks = setOf(
    "checkstyleAot", "checkstyleAotTest",
    "pmdAot", "pmdAotTest",
    "spotbugsAot", "spotbugsAotTest",
)
tasks.matching { it.name in aotQualityTasks }.configureEach {
    enabled = false
}

