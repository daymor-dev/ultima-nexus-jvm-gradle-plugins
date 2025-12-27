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

package dev.daymor.ultimanexus.jvm.gradle.bundle

/**
 * Bundle plugin for simple Spring Boot library development.
 * Applies simple Java conventions with Spring Boot support.
 * Does not include quality checks, reporting, or publishing.
 *
 * Usage:
 * plugins {
 *     id("dev.daymor.ultimanexus.jvm.gradle.bundle.spring-boot-simple")
 * }
 */
plugins {
    id("dev.daymor.ultimanexus.jvm.gradle.bundle.java-simple")
    id("dev.daymor.ultimanexus.jvm.gradle.feature.spring-boot")
    id("dev.daymor.ultimanexus.jvm.gradle.dependency.spring-boot-test")
}
