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

plugins {
    `kotlin-dsl`
    alias(libs.plugins.ultimanexus.jvm.gradle.plugin)
}

dependencies {
    implementation(libs.ultima.nexus.jvm.core)
    implementation(libs.ultima.nexus.jvm.base)
    implementation(libs.spotbugs.gradle.plugin)
    implementation(libs.spotless.plugin.gradle)
    implementation(libs.dependency.analysis.gradle.plugin)
    implementation(libs.classpath.collision.detector)
}

testing.suites.named<JvmTestSuite>("test") {
    useJUnitJupiter()
    dependencies {
        implementation(libs.junit.jupiter.params)
        implementation(libs.assertj.core)
        implementation(libs.mockk)
    }
}
