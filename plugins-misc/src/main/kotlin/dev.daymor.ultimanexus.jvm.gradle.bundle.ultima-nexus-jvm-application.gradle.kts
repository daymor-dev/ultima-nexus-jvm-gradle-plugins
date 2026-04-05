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
import dev.daymor.ultimanexus.jvm.gradle.config.Defaults.UltimaNexusJvm.ApplicationType
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
 *
 * The processor is selected automatically based on the application type
 * and whether Hibernate support is enabled.
 *
 * Configuration (gradle.properties):
 *   ultimaNexusJvm.applicationType=spring-rest-api  (default)
 *     Supported types:
 *       - spring-rest-api : Spring Boot REST API with Spring Data JPA
 *       - entity-only     : Only entity generation (no Spring layers)
 *   ultimaNexusJvm.usePredefinedStarter=true   (default: true)
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
}

if (file("antora-playbook.yml").exists()) {
    apply(plugin = PluginIds.Bundle.DOCUMENTATION)
}

val applicationType = project.findPropertyOrNull(PropertyKeys.UltimaNexusJvm.APPLICATION_TYPE)
    ?: Defaults.UltimaNexusJvm.DEFAULT_APPLICATION_TYPE
val usePredefined = project.findPropertyOrNull(PropertyKeys.UltimaNexusJvm.USE_PREDEFINED_STARTER)
    ?.toBoolean() ?: Defaults.UltimaNexusJvm.DEFAULT_USE_PREDEFINED_STARTER
val useHibernate = project.findPropertyOrNull(PropertyKeys.UltimaNexusJvm.USE_HIBERNATE)
    ?.toBoolean() ?: Defaults.UltimaNexusJvm.DEFAULT_USE_HIBERNATE

val libs = getLibsCatalogOrNull(project)

val starterDep = libs?.let { getLibraryOrNull(it, CatalogLibrary.STARTER) }
val annotationDep = libs?.let { getLibraryOrNull(it, CatalogLibrary.ANNOTATION) }
val processorDep = libs?.let { getLibraryOrNull(it, CatalogLibrary.PROCESSOR) }

fun resolveProcessorFallback(type: String, hibernate: Boolean): String =
    when (type) {
        ApplicationType.SPRING_REST_API -> if (hibernate) {
            Fallbacks.ULTIMA_NEXUS_JVM_PROCESSOR_SPRING_HIBERNATE
        } else {
            Fallbacks.ULTIMA_NEXUS_JVM_PROCESSOR_SPRING_APPLICATION
        }
        ApplicationType.ENTITY_ONLY -> if (hibernate) {
            Fallbacks.ULTIMA_NEXUS_JVM_PROCESSOR_JAVA_HIBERNATE
        } else {
            Fallbacks.ULTIMA_NEXUS_JVM_PROCESSOR_JAVA
        }
        else -> error(
            "Unknown ${PropertyKeys.UltimaNexusJvm.APPLICATION_TYPE}: '$type'. " +
                "Supported types: ${ApplicationType.SUPPORTED.joinToString()}"
        )
    }

fun resolveStarterFallback(type: String, predefined: Boolean): String? =
    when (type) {
        ApplicationType.SPRING_REST_API -> if (predefined) {
            Fallbacks.ULTIMA_NEXUS_JVM_STARTER_PREDEFINED_REST_API
        } else {
            Fallbacks.ULTIMA_NEXUS_JVM_STARTER_REST_API
        }
        ApplicationType.ENTITY_ONLY -> null
        else -> error(
            "Unknown ${PropertyKeys.UltimaNexusJvm.APPLICATION_TYPE}: '$type'. " +
                "Supported types: ${ApplicationType.SUPPORTED.joinToString()}"
        )
    }

dependencies {
    if (starterDep != null) {
        add(DependencyScope.IMPLEMENTATION, starterDep)
    } else {
        val starterFallback = resolveStarterFallback(applicationType, usePredefined)
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
            resolveProcessorFallback(applicationType, useHibernate)
        )
    }
}
