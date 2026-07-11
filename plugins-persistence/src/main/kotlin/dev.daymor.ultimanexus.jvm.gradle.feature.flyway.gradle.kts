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

import org.flywaydb.gradle.task.AbstractFlywayTask

/**
 * Wires Flyway's build-time tasks (`flywayMigrate`, `flywayInfo`,
 * `flywayValidate`, ...) to a directory of versioned migration scripts, so a
 * CI or pre-deploy step can migrate and validate a real database from those
 * scripts without running the application.
 *
 * This is a self-contained convention plugin with no dependency on any
 * particular application framework or fixed script location. Point
 * `flywayBuildTime.migrationsDir` at any directory of versioned `V…__….sql`
 * scripts. The default is `build/classes/java/main/META-INF/db/migration` —
 * where a code generator that emits migrations during `compileJava` places
 * them — so a project whose migrations are generated needs no configuration,
 * while a project with hand-written scripts overrides the one property.
 *
 * Every Flyway task is ordered after `compileJava` so a generated migrations
 * directory is populated first; for a project with static scripts the compile
 * dependency is a harmless no-op.
 *
 * The target database is supplied through the standard Flyway extension
 * (`flyway { url = ...; user = ...; password = ... }`) or, for CI, through
 * the `flyway.url` / `flyway.user` / `flyway.password` Gradle properties
 * (`-Pflyway.url=...`), which this plugin reads as conventions.
 *
 * Usage:
 * ```kotlin
 * plugins {
 *     id("dev.daymor.ultimanexus.jvm.gradle.feature.flyway")
 * }
 *
 * // Only when the scripts are not at the generated-code default:
 * flywayBuildTime {
 *     migrationsDir = layout.projectDirectory.dir("src/main/resources/db/migration")
 * }
 * ```
 * ```bash
 * ./gradlew flywayMigrate -Pflyway.url=jdbc:postgresql://localhost/app \
 *     -Pflyway.user=app -Pflyway.password=secret
 * ```
 */

plugins {
    java
    id("org.flywaydb.flyway")
}

abstract class FlywayBuildTimeExtension {
    abstract val migrationsDir: DirectoryProperty
}

val config = extensions.create<FlywayBuildTimeExtension>("flywayBuildTime")
config.migrationsDir.convention(layout.buildDirectory.dir("classes/java/main/META-INF/db/migration"))

flyway {
    locations = arrayOf("filesystem:" + config.migrationsDir.get().asFile.path)
    (project.findProperty("flyway.url") as String?)?.let { url = it }
    (project.findProperty("flyway.user") as String?)?.let { user = it }
    (project.findProperty("flyway.password") as String?)?.let { password = it }
}

tasks.withType<AbstractFlywayTask>().configureEach { dependsOn(tasks.named("compileJava")) }
