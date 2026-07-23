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
import dev.daymor.ultimanexus.jvm.gradle.config.Defaults.DependencyScope
import dev.daymor.ultimanexus.jvm.gradle.config.Defaults.UltimaNexusJvm.Archetype
import dev.daymor.ultimanexus.jvm.gradle.config.Defaults.UltimaNexusJvm.CatalogLibrary
import dev.daymor.ultimanexus.jvm.gradle.config.PluginIds
import dev.daymor.ultimanexus.jvm.gradle.config.PropertyKeys
import dev.daymor.ultimanexus.jvm.gradle.util.DependencyUtils.Fallbacks
import dev.daymor.ultimanexus.jvm.gradle.util.DependencyUtils.getLibraryOrNull
import dev.daymor.ultimanexus.jvm.gradle.util.DependencyUtils.getLibsCatalogOrNull
import dev.daymor.ultimanexus.jvm.gradle.util.PropertyUtils.findPropertyOrNull

/**
 * Plugin: dev.daymor.ultimanexus.jvm.gradle.bundle.ultima-nexus-jvm-application
 *
 * All-in-one bundle for Ultima Nexus JVM applications. Includes:
 *   - Full Spring Boot native application setup (quality, testing, reports, GraalVM)
 *   - Lombok annotation processing
 *   - Ultima Nexus starter dependency (implementation)
 *   - Ultima Nexus annotation module (compileOnly)
 *   - Ultima Nexus annotation processor (annotationProcessor)
 *   - Documentation (auto-detected via antora-playbook.yml)
 *   - Schema documentation collection (opt-in generateSchemaDocs task)
 *
 * The starter and the processor are both selected from the archetype (and,
 * for the classic/entity-only shapes, whether Hibernate support is enabled).
 *
 * Configuration (gradle.properties):
 *   ultimaNexusJvm.archetype=classic-backend  (default)
 *     Supported archetypes:
 *       - classic-backend / classic-fullstack         : Spring MVC + JPA
 *       - performance-backend / performance-fullstack : throughput-tuned, jOOQ persistence
 *       - reactive-backend / reactive-fullstack       : WebFlux + R2DBC
 *       - base        : the hexagonal core only, no opinionated wiring
 *       - entity-only : entity generation only (no application layers, no starter)
 *   ultimaNexusJvm.useHibernate=true            (default: true)
 *
 * Override dependencies via version catalog entries:
 *   ultima-nexus-jvm-starter       -> overrides starter (implementation)
 *   ultima-nexus-jvm-annotation    -> overrides annotation (compileOnly)
 *   ultima-nexus-jvm-processor     -> overrides processor (annotationProcessor)
 *
 * Usage (minimal — no dependencies block needed):
 * ```kotlin
 * plugins {
 *     id("dev.daymor.ultimanexus.jvm.gradle.bundle.ultima-nexus-jvm-application")
 * }
 * ```
 */
plugins {
    id("dev.daymor.ultimanexus.jvm.gradle.bundle.spring-boot-native-complete-application")
    id("dev.daymor.ultimanexus.jvm.gradle.bundle.lombok")
    id("dev.daymor.ultimanexus.jvm.gradle.feature.mapstruct-spring")
    id("dev.daymor.ultimanexus.jvm.gradle.feature.schema-docs")
}

if (file("antora-playbook.yml").exists()) {
    apply(plugin = PluginIds.Bundle.DOCUMENTATION)
}

val archetype = project.findPropertyOrNull(PropertyKeys.UltimaNexusJvm.ARCHETYPE)
    ?: Defaults.UltimaNexusJvm.DEFAULT_ARCHETYPE
val useHibernate = project.findPropertyOrNull(PropertyKeys.UltimaNexusJvm.USE_HIBERNATE)
    ?.toBoolean() ?: Defaults.UltimaNexusJvm.DEFAULT_USE_HIBERNATE

require(archetype in Archetype.SUPPORTED) {
    "Unknown ${PropertyKeys.UltimaNexusJvm.ARCHETYPE}: '$archetype'. " +
        "Supported archetypes: ${Archetype.SUPPORTED.joinToString()}"
}

if (archetype in Archetype.PERFORMANCE) {
    apply(plugin = PluginIds.Feature.DSLJSON_CODEGEN)
}

val libs = getLibsCatalogOrNull(project)

val starterDep = libs?.let { getLibraryOrNull(it, CatalogLibrary.STARTER) }
val annotationDep = libs?.let { getLibraryOrNull(it, CatalogLibrary.ANNOTATION) }
val processorDep = libs?.let { getLibraryOrNull(it, CatalogLibrary.PROCESSOR) }

fun resolveProcessorFallback(archetype: String, hibernate: Boolean): String =
    when (archetype) {
        in Archetype.REACTIVE -> Fallbacks.ULTIMA_NEXUS_JVM_PROCESSOR_SPRING_APPLICATION
        Archetype.ENTITY_ONLY -> if (hibernate) {
            Fallbacks.ULTIMA_NEXUS_JVM_PROCESSOR_JAVA_HIBERNATE
        } else {
            Fallbacks.ULTIMA_NEXUS_JVM_PROCESSOR_JAVA
        }
        else -> if (hibernate) {
            Fallbacks.ULTIMA_NEXUS_JVM_PROCESSOR_SPRING_HIBERNATE
        } else {
            Fallbacks.ULTIMA_NEXUS_JVM_PROCESSOR_SPRING_APPLICATION
        }
    }

fun resolveStarterFallback(archetype: String): String? =
    when (archetype) {
        Archetype.CLASSIC_BACKEND -> Fallbacks.ULTIMA_NEXUS_JVM_STARTER_CLASSIC_BACKEND
        Archetype.CLASSIC_FULLSTACK -> Fallbacks.ULTIMA_NEXUS_JVM_STARTER_CLASSIC_FULLSTACK
        Archetype.PERFORMANCE_BACKEND -> Fallbacks.ULTIMA_NEXUS_JVM_STARTER_PERFORMANCE_BACKEND
        Archetype.PERFORMANCE_FULLSTACK -> Fallbacks.ULTIMA_NEXUS_JVM_STARTER_PERFORMANCE_FULLSTACK
        Archetype.REACTIVE_BACKEND -> Fallbacks.ULTIMA_NEXUS_JVM_STARTER_REACTIVE_BACKEND
        Archetype.REACTIVE_FULLSTACK -> Fallbacks.ULTIMA_NEXUS_JVM_STARTER_REACTIVE_FULLSTACK
        Archetype.BASE -> Fallbacks.ULTIMA_NEXUS_JVM_STARTER_BASE
        else -> null
    }

dependencies {
    if (starterDep != null) {
        add(DependencyScope.IMPLEMENTATION, starterDep)
    } else {
        val starterFallback = resolveStarterFallback(archetype)
        if (starterFallback != null) {
            add(DependencyScope.IMPLEMENTATION, starterFallback)
        }
    }

    if (annotationDep != null) {
        add(DependencyScope.COMPILE_ONLY, annotationDep)
    } else {
        add(DependencyScope.COMPILE_ONLY, Fallbacks.ULTIMA_NEXUS_JVM_ANNOTATION)
    }

    if (processorDep != null) {
        add(DependencyScope.ANNOTATION_PROCESSOR, processorDep)
    } else {
        add(
            DependencyScope.ANNOTATION_PROCESSOR,
            resolveProcessorFallback(archetype, useHibernate)
        )
    }
}
