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

import org.liquibase.gradle.LiquibaseTask

/**
 * Wires Liquibase's build-time tasks (`update`, `status`, `validate`, ...) to
 * a changelog file, so a CI or pre-deploy step can update and validate a real
 * database from that changelog without running the application.
 *
 * This is a self-contained convention plugin with no dependency on any
 * particular application framework or fixed changelog location. Point
 * `liquibaseBuildTime.changelogFile` at any Liquibase changelog. The default
 * is `build/classes/java/main/META-INF/db/changelog/db.changelog-master.sql` —
 * where a code generator that emits a formatted-SQL changelog during
 * `compileJava` places it — so a project whose changelog is generated needs no
 * configuration, while a project with a hand-written changelog overrides the
 * one property.
 *
 * Every Liquibase task is ordered after `compileJava` so a generated changelog
 * is populated first; for a project with a static changelog the compile
 * dependency is a harmless no-op.
 *
 * The target database is supplied through the `liquibase.url` /
 * `liquibase.user` / `liquibase.password` Gradle properties
 * (`-Pliquibase.url=...`), read here as the `main` activity arguments. The
 * consuming project adds its JDBC driver to the `liquibaseRuntime`
 * configuration (Liquibase core and picocli are added here); and, to get
 * tasks named `liquibaseUpdate` rather than the bare `update`, sets
 * `liquibaseTaskPrefix=liquibase` in its `gradle.properties` — the prefix
 * the Liquibase Gradle plugin reads at apply time.
 *
 * Usage:
 * ```kotlin
 * plugins {
 *     id("dev.daymor.ultimanexus.jvm.gradle.feature.liquibase")
 * }
 *
 * dependencies {
 *     liquibaseRuntime("org.postgresql:postgresql")
 * }
 *
 * // Only when the changelog is not at the generated-code default:
 * liquibaseBuildTime {
 *     changelogFile = layout.projectDirectory.file("src/main/resources/db/changelog.sql")
 * }
 * ```
 * ```properties
 * # gradle.properties
 * liquibaseTaskPrefix=liquibase
 * ```
 * ```bash
 * ./gradlew liquibaseUpdate -Pliquibase.url=jdbc:postgresql://localhost/app \
 *     -Pliquibase.user=app -Pliquibase.password=secret
 * ```
 */

plugins {
    java
    id("org.liquibase.gradle")
}

abstract class LiquibaseBuildTimeExtension {
    abstract val changelogFile: RegularFileProperty
}

val config = extensions.create<LiquibaseBuildTimeExtension>("liquibaseBuildTime")
config.changelogFile.convention(
    layout.buildDirectory.file("classes/java/main/META-INF/db/changelog/db.changelog-master.sql")
)

dependencies {
    "liquibaseRuntime"("org.liquibase:liquibase-core")
    "liquibaseRuntime"("info.picocli:picocli")
}

liquibase {
    activities.register("main") {
        this.arguments = mapOf(
            "changelogFile" to config.changelogFile.get().asFile.path,
            "url" to (project.findProperty("liquibase.url") ?: ""),
            "username" to (project.findProperty("liquibase.user") ?: ""),
            "password" to (project.findProperty("liquibase.password") ?: ""),
        )
    }
    runList = "main"
}

tasks.withType<LiquibaseTask>().configureEach { dependsOn(tasks.named("compileJava")) }
