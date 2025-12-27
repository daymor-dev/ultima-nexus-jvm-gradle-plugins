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

package dev.daymor.ultimanexus.jvm.gradle.dependency

import dev.daymor.ultimanexus.jvm.gradle.util.DependencyUtils.Fallbacks
import dev.daymor.ultimanexus.jvm.gradle.util.DependencyUtils.getLibraryOrNull

plugins {
    java
    id("dev.daymor.ultimanexus.jvm.gradle.base.dependency-rules")
}

val libs: VersionCatalog = versionCatalogs.named("libs")

dependencies {
    val lombokDep = getLibraryOrNull(libs, "lombok") ?: Fallbacks.LOMBOK
    compileOnly(lombokDep)
    annotationProcessor(lombokDep)
}
