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

import dev.daymor.ultimanexus.jvm.gradle.util.DependencyUtils.Fallbacks
import dev.daymor.ultimanexus.jvm.gradle.util.DependencyUtils.getLibraryOrNull
import dev.daymor.ultimanexus.jvm.gradle.util.DependencyUtils.getLibsCatalogOrNull
import javax.inject.Inject
import org.jooq.codegen.gradle.CodegenTask
import org.jooq.meta.jaxb.ForcedType

/**
 * Runs jOOQ's code generator offline against a directory of SQL DDL,
 * generating the type-safe `Tables.*` with no database and no Docker.
 *
 * This is a self-contained convention plugin: it has no dependency on the
 * Ultima Nexus framework, its annotation processor, or any fixed schema
 * location. Point it at any directory of SQL the jOOQ `DDLDatabase` parser
 * understands — Flyway versioned scripts, a Liquibase formatted-SQL
 * changelog, or plain `CREATE TABLE` files — and it generates jOOQ sources
 * into `build/generated-sources/jooq`, wired onto the main source set and
 * ahead of `compileJava`. On top of the bare `org.jooq.jooq-codegen-gradle`
 * plugin it adds the offline `DDLDatabase` wiring, a single-property
 * `scriptsDir`, configuration-cache-safe configuration, and the common
 * generator overrides (package, include / exclude, forced types).
 *
 * Usage:
 * ```kotlin
 * plugins {
 *     id("dev.daymor.ultimanexus.jvm.gradle.feature.jooq-codegen")
 * }
 *
 * jooqCodegenConfig {
 *     scriptsDir = "src/main/resources/db/migration"
 *     basePackage = "com.example.app.jooq"
 *     // Optional jOOQ generator overrides:
 *     excludes = "audit_log"
 *     forcedType {
 *         userType = "com.example.Money"
 *         converter = "com.example.MoneyConverter"
 *         includeExpression = ".*\\.amount"
 *     }
 * }
 * ```
 *
 * A consumer whose schema is itself produced at build time (for example by
 * an annotation processor that emits migrations) generates that SQL into a
 * directory first and points `scriptsDir` at it, depending the codegen on
 * that generation step. The plugin stays unaware of how the SQL was produced.
 */

plugins {
    java
    id("org.jooq.jooq-codegen-gradle")
}

interface JooqForcedType {
    val includeExpression: Property<String>

    val includeTypes: Property<String>

    val name: Property<String>

    val userType: Property<String>

    val converter: Property<String>
}

abstract class JooqCodegenConfigExtension {
    abstract val scriptsDir: Property<String>

    abstract val basePackage: Property<String>

    abstract val includes: Property<String>

    abstract val excludes: Property<String>

    abstract val forcedTypes: ListProperty<JooqForcedType>

    @get:Inject
    abstract val objects: ObjectFactory

    fun forcedType(action: Action<JooqForcedType>) {
        val forced = objects.newInstance(JooqForcedType::class.java)
        action.execute(forced)
        forcedTypes.add(forced)
    }
}

val config = extensions.create<JooqCodegenConfigExtension>("jooqCodegenConfig")
config.scriptsDir.convention("src/main/resources/db/migration")
config.basePackage.convention("${project.group}.jooq")

val jooqMetaExtensions: Any =
    getLibsCatalogOrNull(project)?.let { getLibraryOrNull(it, "jooq-meta-extensions") }
        ?: Fallbacks.JOOQ_META_EXTENSIONS
dependencies {
    "jooqCodegen"(jooqMetaExtensions)
}

val migrationScripts = "**.sql"
val generatedJooqDir = layout.buildDirectory.dir("generated-sources/jooq").get().asFile.path

jooq {
    configuration {
        generator {
            target {
                setDirectory(generatedJooqDir)
            }
        }
    }
}

tasks.named<JavaCompile>("compileJava") {
    dependsOn(tasks.named("jooqCodegen"))
}

plugins.withId("com.autonomousapps.dependency-analysis") {
    afterEvaluate {
        tasks.named("explodeCodeSourceMain") {
            dependsOn(tasks.named("jooqCodegen"))
        }
    }
}

tasks.withType<Checkstyle>().configureEach { exclude("**/jooq/**") }

afterEvaluate {
    val scriptsGlob = layout.projectDirectory.dir(config.scriptsDir.get()).asFile.path + "/" + migrationScripts
    val resolvedPackage = config.basePackage.get()
    val resolvedIncludes = config.includes.orNull
    val resolvedExcludes = config.excludes.orNull
    val resolvedForcedTypes = config.forcedTypes.get().map { forced ->
        ForcedType().apply {
            forced.name.orNull?.let { setName(it) }
            forced.userType.orNull?.let { setUserType(it) }
            forced.converter.orNull?.let { setConverter(it) }
            forced.includeExpression.orNull?.let { setIncludeExpression(it) }
            forced.includeTypes.orNull?.let { setIncludeTypes(it) }
        }
    }
    jooq {
        configuration {
            generator {
                database {
                    setName("org.jooq.meta.extensions.ddl.DDLDatabase")
                    properties {
                        property {
                            setKey("scripts")
                            setValue(scriptsGlob)
                        }
                        property {
                            setKey("sort")
                            setValue("flyway")
                        }
                        property {
                            setKey("defaultNameCase")
                            setValue("lower")
                        }
                        property {
                            setKey("unqualifiedSchema")
                            setValue("none")
                        }
                    }
                    resolvedIncludes?.let { setIncludes(it) }
                    resolvedExcludes?.let { setExcludes(it) }
                    if (resolvedForcedTypes.isNotEmpty()) {
                        forcedTypes.addAll(resolvedForcedTypes)
                    }
                }
                target {
                    setPackageName(resolvedPackage)
                }
            }
        }
    }
    tasks.named<CodegenTask>("jooqCodegen") {
        inputs.dir(layout.projectDirectory.dir(config.scriptsDir.get()))
            .withPropertyName("scriptsDir").optional()
    }
}
