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

package dev.daymor.ultimanexus.jvm.gradle.util

import dev.daymor.ultimanexus.jvm.gradle.config.Defaults
import dev.daymor.ultimanexus.jvm.gradle.config.Messages
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.provider.Provider

object DependencyUtils {

    fun getLibsCatalogOrNull(project: Project): VersionCatalog? =
        try {
            project.extensions.findByType(VersionCatalogsExtension::class.java)
                ?.find(Defaults.VERSION_CATALOG_NAME)
                ?.orElse(null)
        } catch (_: Exception) {
            null
        }

    fun getLibsCatalog(project: Project): VersionCatalog =
        getLibsCatalogOrNull(project) ?: throw GradleException(Messages.VERSION_CATALOG_NOT_FOUND)

    object FallbackVersions {
        const val ASSERTJ = "3.27.7"
        const val BYTE_BUDDY_AGENT = "1.18.7"
        const val CHECKSTYLE = "12.3.0"
        const val ECLIPSE_JDT = "4.38"
        const val JMH = "1.37"
        const val JSPECIFY = "1.0.0"
        const val JSR305 = "3.0.2"
        const val JUNIT_JUPITER = "6.0.3"
        const val LOMBOK = "1.18.44"
        const val LOMBOK_MAPSTRUCT_BINDING = "0.2.0"
        const val MAPSTRUCT = "1.6.3"
        const val MOCKK = "1.14.9"
        const val PMD = "7.19.0"
        const val SLF4J = "2.0.17"
        const val SPRING_BOOT = "4.1.0"
        const val ULTIMA_NEXUS_JVM = "0.2.0-SNAPSHOT"
        const val ULTIMA_NEXUS_JVM_CHECK = "1.0.0"
        const val HTMX_SPRING_BOOT = "5.0.0"
        const val VAADIN = "25.2.1"
        const val JOOQ = "3.21.5"
        const val THYMELEAF = "3.1.5.RELEASE"
        const val HTMX_WEBJAR = "2.0.10"
    }

    object Fallbacks {
        const val ASSERTJ_CORE = "org.assertj:assertj-core:${FallbackVersions.ASSERTJ}"
        const val BYTE_BUDDY_AGENT = "net.bytebuddy:byte-buddy-agent:${FallbackVersions.BYTE_BUDDY_AGENT}"
        const val JSPECIFY = "org.jspecify:jspecify:${FallbackVersions.JSPECIFY}"
        const val JSR305 = "com.google.code.findbugs:jsr305:${FallbackVersions.JSR305}"
        const val JUNIT_JUPITER_ENGINE = "org.junit.jupiter:junit-jupiter-engine:${FallbackVersions.JUNIT_JUPITER}"
        const val JUNIT_JUPITER_PARAMS = "org.junit.jupiter:junit-jupiter-params:${FallbackVersions.JUNIT_JUPITER}"
        const val LOMBOK = "org.projectlombok:lombok:${FallbackVersions.LOMBOK}"
        const val LOMBOK_MAPSTRUCT_BINDING =
            "org.projectlombok:lombok-mapstruct-binding:${FallbackVersions.LOMBOK_MAPSTRUCT_BINDING}"
        const val MAPSTRUCT = "org.mapstruct:mapstruct:${FallbackVersions.MAPSTRUCT}"
        const val MAPSTRUCT_PROCESSOR = "org.mapstruct:mapstruct-processor:${FallbackVersions.MAPSTRUCT}"
        const val MOCKK = "io.mockk:mockk:${FallbackVersions.MOCKK}"
        const val SLF4J_SIMPLE = "org.slf4j:slf4j-simple:${FallbackVersions.SLF4J}"
        const val SPRING_BOOT_DEVTOOLS = "org.springframework.boot:spring-boot-devtools:${FallbackVersions.SPRING_BOOT}"
        const val SPRING_BOOT_DOCKER_COMPOSE = "org.springframework.boot:spring-boot-docker-compose:${FallbackVersions.SPRING_BOOT}"
        const val SPRING_BOOT_BOM = "org.springframework.boot:spring-boot-dependencies:${FallbackVersions.SPRING_BOOT}"
        const val ULTIMA_NEXUS_JVM_CHECK = "dev.daymor.ultima-nexus.jvm:ultima-nexus-jvm-check:${FallbackVersions.ULTIMA_NEXUS_JVM_CHECK}"

        private const val UN_GROUP = "dev.daymor.ultimanexus.jvm"
        private const val UN_VERSION = FallbackVersions.ULTIMA_NEXUS_JVM
        const val ULTIMA_NEXUS_JVM_STARTER_BASE =
            "$UN_GROUP:starter-spring-base:$UN_VERSION"
        const val ULTIMA_NEXUS_JVM_STARTER_CLASSIC_BACKEND =
            "$UN_GROUP:starter-spring-classic-backend:$UN_VERSION"
        const val ULTIMA_NEXUS_JVM_STARTER_CLASSIC_FULLSTACK =
            "$UN_GROUP:starter-spring-classic-fullstack:$UN_VERSION"
        const val ULTIMA_NEXUS_JVM_STARTER_PERFORMANCE_BACKEND =
            "$UN_GROUP:starter-spring-performance-backend:$UN_VERSION"
        const val ULTIMA_NEXUS_JVM_STARTER_PERFORMANCE_FULLSTACK =
            "$UN_GROUP:starter-spring-performance-fullstack:$UN_VERSION"
        const val ULTIMA_NEXUS_JVM_STARTER_REACTIVE_BACKEND =
            "$UN_GROUP:starter-spring-reactive-backend:$UN_VERSION"
        const val ULTIMA_NEXUS_JVM_STARTER_REACTIVE_FULLSTACK =
            "$UN_GROUP:starter-spring-reactive-fullstack:$UN_VERSION"
        const val ULTIMA_NEXUS_JVM_ANNOTATION =
            "$UN_GROUP:annotation-jvm:$UN_VERSION"
        const val ULTIMA_NEXUS_JVM_PROCESSOR_SPRING_TEST =
            "$UN_GROUP:processor-java-spring-test:$UN_VERSION"
        const val ULTIMA_NEXUS_JVM_STARTER_TEST_BASE =
            "$UN_GROUP:starter-spring-test-base:$UN_VERSION"
        const val ULTIMA_NEXUS_JVM_STARTER_INTEGRATION_TEST =
            "$UN_GROUP:starter-spring-integration-test:$UN_VERSION"
        const val ULTIMA_NEXUS_JVM_STARTER_FUNCTIONAL_TEST =
            "$UN_GROUP:starter-spring-functional-test:$UN_VERSION"
        const val ULTIMA_NEXUS_JVM_STARTER_PERFORMANCE_TEST =
            "$UN_GROUP:starter-spring-performance-test:$UN_VERSION"
        const val JMH_GENERATOR_ANNPROCESS =
            "org.openjdk.jmh:jmh-generator-annprocess:${FallbackVersions.JMH}"
        const val ULTIMA_NEXUS_JVM_PROCESSOR_JAVA =
            "$UN_GROUP:processor-java:$UN_VERSION"
        const val ULTIMA_NEXUS_JVM_PROCESSOR_JAVA_HIBERNATE =
            "$UN_GROUP:processor-java-hibernate:$UN_VERSION"
        const val ULTIMA_NEXUS_JVM_PROCESSOR_SPRING_APPLICATION =
            "$UN_GROUP:processor-java-spring-application:$UN_VERSION"
        const val ULTIMA_NEXUS_JVM_PROCESSOR_SPRING_HIBERNATE =
            "$UN_GROUP:processor-java-spring-hibernate:$UN_VERSION"
        const val ULTIMA_NEXUS_JVM_APPLICATION_CONTRACT_UI =
            "$UN_GROUP:application-contract-ui:$UN_VERSION"
        const val ULTIMA_NEXUS_JVM_CONFIGURATION_SPRING_FRONTEND =
            "$UN_GROUP:configuration-spring-frontend:$UN_VERSION"
        const val ULTIMA_NEXUS_JVM_FRONTEND_VAADIN =
            "$UN_GROUP:frontend-vaadin:$UN_VERSION"
        const val ULTIMA_NEXUS_JVM_FRONTEND_HTMX =
            "$UN_GROUP:frontend-htmx:$UN_VERSION"

        const val THYMELEAF = "org.thymeleaf:thymeleaf:${FallbackVersions.THYMELEAF}"
        const val HTMX_WEBJAR = "org.webjars.npm:htmx.org:${FallbackVersions.HTMX_WEBJAR}"
        const val HTMX_SPRING_BOOT = "io.github.wimdeblauwe:htmx-spring-boot:${FallbackVersions.HTMX_SPRING_BOOT}"
        const val SPRING_BOOT_STARTER_THYMELEAF =
            "org.springframework.boot:spring-boot-starter-thymeleaf:${FallbackVersions.SPRING_BOOT}"
        const val VAADIN_SPRING_BOOT_STARTER =
            "com.vaadin:vaadin-spring-boot-starter:${FallbackVersions.VAADIN}"
        const val JOOQ_META_EXTENSIONS = "org.jooq:jooq-meta-extensions:${FallbackVersions.JOOQ}"
    }

    fun getLibrary(versionCatalog: VersionCatalog, name: String): Provider<MinimalExternalModuleDependency> =
        versionCatalog.findLibrary(name).orElseThrow { GradleException(Messages.libraryNotFound(name)) }

    fun getLibraryOrNull(versionCatalog: VersionCatalog, name: String): Provider<MinimalExternalModuleDependency>? =
        versionCatalog.findLibrary(name).orElse(null)

    fun getVersion(versionCatalog: VersionCatalog, name: String): String =
        versionCatalog.findVersion(name).orElseThrow { GradleException(Messages.versionNotFound(name)) }.toString()

    fun getVersionOrNull(versionCatalog: VersionCatalog, name: String): String? =
        versionCatalog.findVersion(name).orElse(null)?.toString()
}
