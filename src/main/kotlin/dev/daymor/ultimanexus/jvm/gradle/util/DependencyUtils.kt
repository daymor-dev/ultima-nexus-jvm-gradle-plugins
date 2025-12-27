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

import org.gradle.api.GradleException
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.provider.Provider

object DependencyUtils {

    /**
     * Fallback versions for dependencies when not defined in consumer's version catalog.
     * These versions are kept in sync with gradle/libs.versions.toml.
     */
    object Fallbacks {
        const val BYTE_BUDDY_AGENT = "net.bytebuddy:byte-buddy-agent:1.18.3"
        const val JSPECIFY = "org.jspecify:jspecify:1.0.0"
        const val JSR305 = "com.google.code.findbugs:jsr305:3.0.2"
        const val JUNIT_JUPITER_ENGINE = "org.junit.jupiter:junit-jupiter-engine:6.0.1"
        const val JUNIT_JUPITER_VERSION = "6.0.1"
        const val LOMBOK = "org.projectlombok:lombok:1.18.42"
        const val SLF4J_SIMPLE = "org.slf4j:slf4j-simple:2.0.17"
        const val SPRING_BOOT_DEVTOOLS = "org.springframework.boot:spring-boot-devtools:4.0.1"
    }

    fun getLibrary(
        versionCatalog: VersionCatalog,
        name: String,
    ): Provider<MinimalExternalModuleDependency> =
        versionCatalog.findLibrary(name).orElseThrow {
            GradleException(
                "Library '$name' not found in version catalog 'libs'. " +
                    "Add it to your gradle/libs.versions.toml or use a bundle plugin with fallback versions."
            )
        }

    /**
     * Gets a library from the version catalog, or returns null if not found.
     */
    fun getLibraryOrNull(
        versionCatalog: VersionCatalog,
        name: String,
    ): Provider<MinimalExternalModuleDependency>? =
        versionCatalog.findLibrary(name).orElse(null)

    fun getVersion(versionCatalog: VersionCatalog, name: String): String =
        versionCatalog.findVersion(name).orElseThrow {
            GradleException(
                "Version '$name' not found in version catalog 'libs'. " +
                    "Add it to your gradle/libs.versions.toml."
            )
        }.toString()

    /**
     * Gets a version from the version catalog, or returns null if not found.
     */
    fun getVersionOrNull(versionCatalog: VersionCatalog, name: String): String? =
        versionCatalog.findVersion(name).orElse(null)?.toString()
}
