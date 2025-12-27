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

import dev.daymor.ultimanexus.jvm.gradle.util.DependencyUtils.getLibrary

plugins {
    java
    id("org.springframework.boot")
    id("dev.daymor.ultimanexus.jvm.gradle.base.dependency-rules")
}

// Configure after evaluation when Spring Boot has created its configurations
afterEvaluate {
    configurations {
        val internalConfig = configurations.findByName("internal")
        val mainRuntimeConfig = configurations.findByName("mainRuntimeClasspath")

        productionRuntimeClasspath {
            internalConfig?.let { extendsFrom(it) }
            mainRuntimeConfig?.let { shouldResolveConsistentlyWith(it) }
        }
        findByName("developmentOnly")?.apply {
            internalConfig?.let { extendsFrom(it) }
            mainRuntimeConfig?.let { shouldResolveConsistentlyWith(it) }
        }
    }
}

val libs: VersionCatalog = versionCatalogs.named("libs")

dependencies {
    compileOnly(getLibrary(libs, "jspecify"))
    compileOnly(getLibrary(libs, "jsr305"))
    developmentOnly(getLibrary(libs, "spring-boot-devtools"))
}
