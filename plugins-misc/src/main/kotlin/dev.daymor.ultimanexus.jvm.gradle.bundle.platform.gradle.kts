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


/**
 * Bundle plugin for BOM/platform projects.
 * Applies Java Platform with version catalog constraints, project identity,
 * and version consistency checking.
 *
 * Included plugins:
 *   - dev.daymor.ultimanexus.jvm.gradle.feature.version-platform
 *   - dev.daymor.ultimanexus.jvm.gradle.base.identity
 *   - dev.daymor.ultimanexus.jvm.gradle.check.dependency-versions
 *
 * Usage:
 *   plugins {
 *       alias(libs.plugins.ultimanexus.jvm.bundle.platform)
 *   }
 *
 * Note: This bundle applies java-platform and cannot be combined with java or java-library plugins.
 */
plugins {
    id("dev.daymor.ultimanexus.jvm.gradle.feature.version-platform")
    id("dev.daymor.ultimanexus.jvm.gradle.base.identity")
    id("dev.daymor.ultimanexus.jvm.gradle.check.dependency-versions")
}
