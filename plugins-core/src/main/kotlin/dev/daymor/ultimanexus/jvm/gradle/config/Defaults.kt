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

package dev.daymor.ultimanexus.jvm.gradle.config

object Defaults {
    const val JDK_VERSION = 25
    const val PLATFORM_PATH = ":versions"
    const val AGGREGATION_PATH = ":"
    const val SPOTBUGS_EFFORT = "MAX"
    const val SPOTBUGS_REPORT_LEVEL = "LOW"
    const val TEST_MAX_HEAP_SIZE = "1g"
    const val CONTEXT_TEST_MAX_HEAP_SIZE = "3g"
    const val TEST_FORK_EVERY = 0L
    const val FILE_ENCODING = "UTF-8"
    const val CONTAINER_REUSE_SYSTEM_PROPERTY = "test.container.reuse"
    val DEFAULT_TEST_SUITES = listOf("integrationTest", "functionalTest", "performanceTest")
    val CONTEXT_BOOTING_SUITES = listOf("integrationTest", "functionalTest")
    val SUITES_WITHOUT_BYTEBUDDY = listOf("performanceTest")
    val SUITES_WITHOUT_JACOCO = listOf("performanceTest")
    const val PROJECT_STRUCTURE_DEPTH = 1
    val PROJECT_STRUCTURE_EXCLUSIONS = listOf("gradle/plugins", "*-gradle-plugins", "build")
    const val SHARED_GRADLE_PATH = "../gradle/"
    const val SHARED_PROPERTIES_FILE = "shared.properties"
    const val VERSION_CATALOG_NAME = "libs"
    const val BUILD_GRADLE_KTS = "build.gradle.kts"
    const val SETTINGS_GRADLE_KTS = "settings.gradle.kts"
    const val GRADLE_KTS_EXTENSION = ".gradle.kts"

    object Antora {
        val STATIC_FILE_PATTERNS = listOf("*.html", "*.png", "*.ico", "*.svg", "*.jpg", "*.webp")
    }

    object Benchmark {
        const val DEFAULT_THRESHOLD_PERCENT = 20.0
        const val BASELINE_DIRECTORY = "perf-baselines"
        const val RESULT_DIRECTORY = "reports/jmh"
    }

    object DependencyScope {
        const val IMPLEMENTATION = "implementation"
        const val API = "api"
        const val COMPILE_ONLY = "compileOnly"
        const val RUNTIME_ONLY = "runtimeOnly"
        const val ANNOTATION_PROCESSOR = "annotationProcessor"
        const val TEST_IMPLEMENTATION = "testImplementation"
    }

    object TaskGroup {
        const val BUILD = "build"
        const val VERIFICATION = "verification"
        const val VERIFICATION_OTHER = "verification.other"
        const val DOCUMENTATION = "documentation"
        const val PUBLISHING = "publishing"
        const val PUBLISHING_OTHER = "publishing.other"
    }

    object ConfigurationName {
        const val BYTE_BUDDY_AGENT = "byteBuddyAgent"
        const val CHECKSTYLE_CHECK_ARTIFACT = "checkstyleCheckArtifact"
        const val PMD_CHECK_ARTIFACT = "pmdCheckArtifact"
        const val SPOTBUGS_CHECK_ARTIFACT = "spotbugsCheckArtifact"
        const val FORMAT_CHECK_ARTIFACT = "formatCheckArtifact"
    }

    object UltimaNexusJvm {

        object Archetype {
            const val CLASSIC_BACKEND = "classic-backend"
            const val CLASSIC_FULLSTACK = "classic-fullstack"
            const val PERFORMANCE_BACKEND = "performance-backend"
            const val PERFORMANCE_FULLSTACK = "performance-fullstack"
            const val REACTIVE_BACKEND = "reactive-backend"
            const val REACTIVE_FULLSTACK = "reactive-fullstack"
            const val BASE = "base"
            const val ENTITY_ONLY = "entity-only"

            val SUPPORTED = listOf(
                CLASSIC_BACKEND, CLASSIC_FULLSTACK,
                PERFORMANCE_BACKEND, PERFORMANCE_FULLSTACK,
                REACTIVE_BACKEND, REACTIVE_FULLSTACK,
                BASE, ENTITY_ONLY
            )

            val REACTIVE = setOf(REACTIVE_BACKEND, REACTIVE_FULLSTACK)
        }

        const val DEFAULT_ARCHETYPE = Archetype.CLASSIC_BACKEND
        const val DEFAULT_USE_HIBERNATE = true

        object CatalogLibrary {
            const val STARTER = "ultima-nexus-jvm-starter"
            const val ANNOTATION = "ultima-nexus-jvm-annotation"
            const val PROCESSOR = "ultima-nexus-jvm-processor"
            const val TEST_PROCESSOR = "ultima-nexus-jvm-test-processor"
            const val STARTER_TEST_BASE = "ultima-nexus-jvm-starter-test-base"
            const val STARTER_INTEGRATION_TEST = "ultima-nexus-jvm-starter-integration-test"
            const val STARTER_FUNCTIONAL_TEST = "ultima-nexus-jvm-starter-functional-test"
            const val STARTER_PERFORMANCE_TEST = "ultima-nexus-jvm-starter-performance-test"
            const val JMH_GENERATOR_ANNPROCESS = "jmh-generator-annprocess"
            const val CONTRACT_UI = "ultima-nexus-jvm-application-contract-ui"
            const val CONFIGURATION_FRONTEND = "ultima-nexus-jvm-configuration-spring-frontend"
            const val FRONTEND_VAADIN = "ultima-nexus-jvm-frontend-vaadin"
            const val FRONTEND_HTMX = "ultima-nexus-jvm-frontend-htmx"
            const val THYMELEAF = "thymeleaf"
            const val HTMX_WEBJAR = "htmx-webjar"
            const val HTMX_SPRING_BOOT = "htmx-spring-boot"
            const val SPRING_BOOT_STARTER_THYMELEAF = "spring-boot-starter-thymeleaf"
            const val VAADIN_SPRING_BOOT_STARTER = "vaadin-spring-boot-starter"
        }
    }

    object Frontend {
        const val DEFAULT_VAADIN_PRODUCTION_MODE = true
    }

    object Repositories {
        const val INCLUDE_MAVEN_CENTRAL = true
        const val INCLUDE_MAVEN_LOCAL = false
        const val ALLOW_INSECURE_PROTOCOL = false
    }
}
