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
    const val FILE_ENCODING = "UTF-8"
    val DEFAULT_TEST_SUITES = listOf("integrationTest", "functionalTest", "performanceTest")
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

        object ApplicationType {
            const val SPRING_REST_API = "spring-rest-api"
            const val ENTITY_ONLY = "entity-only"
            val SUPPORTED = listOf(SPRING_REST_API, ENTITY_ONLY)
        }

        const val DEFAULT_APPLICATION_TYPE = ApplicationType.SPRING_REST_API
        const val DEFAULT_USE_PREDEFINED_STARTER = true
        const val DEFAULT_USE_HIBERNATE = true

        object CatalogLibrary {
            const val STARTER = "ultima-nexus-jvm-starter"
            const val ANNOTATION = "ultima-nexus-jvm-annotation"
            const val PROCESSOR = "ultima-nexus-jvm-processor"
        }
    }

    object Repositories {
        const val INCLUDE_MAVEN_CENTRAL = true
        const val INCLUDE_MAVEN_LOCAL = false
        const val ALLOW_INSECURE_PROTOCOL = false
    }
}
