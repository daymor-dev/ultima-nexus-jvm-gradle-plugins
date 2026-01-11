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
 * Plugin that adds Lombok dependency for test source compilation.
 *
 * This plugin configures Lombok as a test compile-only dependency and test annotation
 * processor, enabling the use of Lombok annotations like @Data, @Getter, @Setter,
 * @Builder, etc. in test code.
 *
 * The plugin resolves the Lombok version from the version catalog (libs.versions.toml)
 * using the "lombok" alias, with a fallback version if not defined.
 *
 * Usage:
 * ```kotlin
 * plugins {
 *     alias(libs.plugins.ultimanexus.jvm.dependency.lombok.test)
 * }
 * ```
 *
 * Version catalog configuration (optional):
 * ```toml
 * [libraries]
 * lombok = { module = "org.projectlombok:lombok", version = "1.18.x" }
 * ```
 */

plugins {
    java
    id("dev.daymor.ultimanexus.jvm.gradle.base.dependency-rules")
}

val libs: VersionCatalog? = getLibsCatalogOrNull(project)

dependencies {
    val lombokDep = libs?.let { getLibraryOrNull(it, "lombok") } ?: Fallbacks.LOMBOK
    testCompileOnly(lombokDep)
    testAnnotationProcessor(lombokDep)
}
