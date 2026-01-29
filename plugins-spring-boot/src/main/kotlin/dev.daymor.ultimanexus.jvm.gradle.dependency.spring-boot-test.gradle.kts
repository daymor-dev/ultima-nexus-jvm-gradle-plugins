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

/**
 * Dependency plugin for Spring Boot test dependencies.
 * Adds common test-time dependencies for Spring Boot projects.
 *
 * Adds:
 * - jsr305 (testCompileOnly)
 *
 * Usage:
 * ```kotlin
 * plugins {
 *     alias(libs.plugins.ultimanexus.jvm.dependency.spring.boot.test)
 * }
 * ```
 */

plugins {
    java
    id("dev.daymor.ultimanexus.jvm.gradle.base.dependency-rules")
}

val libs: VersionCatalog? = getLibsCatalogOrNull(project)

dependencies {
    testCompileOnly(libs?.let { getLibraryOrNull(it, "jsr305") } ?: Fallbacks.JSR305)
}
