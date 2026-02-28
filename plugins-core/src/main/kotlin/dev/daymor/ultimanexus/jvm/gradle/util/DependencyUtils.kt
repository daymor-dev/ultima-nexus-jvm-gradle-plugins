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
        const val BYTE_BUDDY_AGENT = "1.18.5"
        const val CHECKSTYLE = "12.3.0"
        const val ECLIPSE_JDT = "4.38"
        const val JSPECIFY = "1.0.0"
        const val JSR305 = "3.0.2"
        const val JUNIT_JUPITER = "6.0.3"
        const val LOMBOK = "1.18.42"
        const val MOCKK = "1.14.9"
        const val PMD = "7.19.0"
        const val SLF4J = "2.0.17"
        const val SPRING_BOOT = "4.0.3"
        const val ULTIMA_NEXUS_JVM_CHECK = "1.0.0"
    }

    object Fallbacks {
        const val ASSERTJ_CORE = "org.assertj:assertj-core:${FallbackVersions.ASSERTJ}"
        const val BYTE_BUDDY_AGENT = "net.bytebuddy:byte-buddy-agent:${FallbackVersions.BYTE_BUDDY_AGENT}"
        const val JSPECIFY = "org.jspecify:jspecify:${FallbackVersions.JSPECIFY}"
        const val JSR305 = "com.google.code.findbugs:jsr305:${FallbackVersions.JSR305}"
        const val JUNIT_JUPITER_ENGINE = "org.junit.jupiter:junit-jupiter-engine:${FallbackVersions.JUNIT_JUPITER}"
        const val JUNIT_JUPITER_PARAMS = "org.junit.jupiter:junit-jupiter-params:${FallbackVersions.JUNIT_JUPITER}"
        const val LOMBOK = "org.projectlombok:lombok:${FallbackVersions.LOMBOK}"
        const val MOCKK = "io.mockk:mockk:${FallbackVersions.MOCKK}"
        const val SLF4J_SIMPLE = "org.slf4j:slf4j-simple:${FallbackVersions.SLF4J}"
        const val SPRING_BOOT_DEVTOOLS = "org.springframework.boot:spring-boot-devtools:${FallbackVersions.SPRING_BOOT}"
        const val SPRING_BOOT_BOM = "org.springframework.boot:spring-boot-dependencies:${FallbackVersions.SPRING_BOOT}"
        const val ULTIMA_NEXUS_JVM_CHECK = "dev.daymor.ultima-nexus.jvm:ultima-nexus-jvm-check:${FallbackVersions.ULTIMA_NEXUS_JVM_CHECK}"
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
