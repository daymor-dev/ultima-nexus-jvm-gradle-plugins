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
    id("com.gradle.plugin-publish") version "2.0.0"
}

group = "dev.daymor.ultima-nexus.jvm.gradle"
version = "1.0.0"

dependencies {
    implementation(libs.antora)
    implementation(libs.classpath.collision.detector)
    implementation(libs.cyclonedx.gradle.plugin)
    implementation(libs.dependency.analysis.gradle.plugin)
    implementation(libs.gradle.pre.commit.git.hooks)
    implementation(libs.gradleup.nmcp.aggregation)
    implementation(libs.jvm.dependency.conflict.resolution)
    implementation(libs.node)
    implementation(libs.spotbugs.gradle.plugin)
    implementation(libs.spotless.plugin.gradle)
    implementation(libs.spring.boot.gradle.plugin)

    testImplementation(libs.junit.jupiter.engine)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

val pluginTags = listOf("jvm", "java", "kotlin", "convention", "quality", "ultima-nexus")

gradlePlugin {
    website = "https://github.com/daymor-dev/ultima-nexus-jvm-gradle-plugins"
    vcsUrl = "https://github.com/daymor-dev/ultima-nexus-jvm-gradle-plugins"
}

// Plugin metadata configuration for Gradle Plugin Portal
// Precompiled script plugins are auto-registered, we configure their metadata here
afterEvaluate {
    gradlePlugin {
        plugins {
            // Base Plugins
            named("dev.daymor.ultimanexus.jvm.gradle.base.identity") {
                displayName = "Ultima Nexus - Project Identity"
                description = "Configures project group ID and identity for JVM projects"
                tags = pluginTags
            }
            named("dev.daymor.ultimanexus.jvm.gradle.base.lifecycle") {
                displayName = "Ultima Nexus - Lifecycle Tasks"
                description = "Adds qualityCheck and qualityGate lifecycle tasks"
                tags = pluginTags
            }
            named("dev.daymor.ultimanexus.jvm.gradle.base.lifecycle.root") {
                displayName = "Ultima Nexus - Root Lifecycle"
                description = "Root project lifecycle task configuration"
                tags = pluginTags
            }
            named("dev.daymor.ultimanexus.jvm.gradle.base.dependency-rules") {
                displayName = "Ultima Nexus - Dependency Rules"
                description = "JVM dependency conflict resolution rules"
                tags = pluginTags
            }

            // Feature Bundle Plugins
            named("dev.daymor.ultimanexus.jvm.gradle.bundle.check") {
                displayName = "Ultima Nexus - Check Bundle"
                description = "Quality checks bundle: Checkstyle, PMD, SpotBugs, and Spotless"
                tags = pluginTags
            }
            named("dev.daymor.ultimanexus.jvm.gradle.bundle.test") {
                displayName = "Ultima Nexus - Test Bundle"
                description = "All test types: unit, integration, functional, and performance"
                tags = pluginTags
            }
            named("dev.daymor.ultimanexus.jvm.gradle.bundle.report") {
                displayName = "Ultima Nexus - Report Bundle"
                description = "Reports: code coverage and SBOM generation"
                tags = pluginTags
            }
            named("dev.daymor.ultimanexus.jvm.gradle.bundle.documentation") {
                displayName = "Ultima Nexus - Documentation Bundle"
                description = "Antora documentation generation"
                tags = pluginTags
            }

            // Project Bundle Plugins
            named("dev.daymor.ultimanexus.jvm.gradle.bundle.gradle-project") {
                displayName = "Ultima Nexus - Gradle Project"
                description = "Base Gradle project conventions"
                tags = pluginTags
            }
            named("dev.daymor.ultimanexus.jvm.gradle.bundle.java-simple") {
                displayName = "Ultima Nexus - Java Simple"
                description = "Simple Java project without quality checks"
                tags = pluginTags
            }
            named("dev.daymor.ultimanexus.jvm.gradle.bundle.java-complete") {
                displayName = "Ultima Nexus - Java Complete"
                description = "Complete Java library with quality, testing, and publishing"
                tags = pluginTags
            }

            // Framework Bundle Plugins
            named("dev.daymor.ultimanexus.jvm.gradle.bundle.spring-boot-simple") {
                displayName = "Ultima Nexus - Spring Boot Simple"
                description = "Simple Spring Boot library without quality checks"
                tags = pluginTags
            }
            named("dev.daymor.ultimanexus.jvm.gradle.bundle.spring-boot-complete") {
                displayName = "Ultima Nexus - Spring Boot Complete"
                description = "Complete Spring Boot library with all features"
                tags = pluginTags
            }
            named("dev.daymor.ultimanexus.jvm.gradle.bundle.spring-boot-application") {
                displayName = "Ultima Nexus - Spring Boot Application"
                description = "Spring Boot application with quality checks and testing"
                tags = pluginTags
            }
            named("dev.daymor.ultimanexus.jvm.gradle.bundle.ultima-nexus") {
                displayName = "Ultima Nexus - Full Bundle"
                description = "Complete Ultima Nexus JVM Framework experience"
                tags = pluginTags
            }

            // Settings Bundle Plugin
            named("dev.daymor.ultimanexus.jvm.gradle.bundle.ultima-nexus-jvm") {
                displayName = "Ultima Nexus - Settings Bundle"
                description = "Settings plugin bundle: project structure and git hooks"
                tags = pluginTags
            }

            // Check Plugins
            named("dev.daymor.ultimanexus.jvm.gradle.check.checkstyle") {
                displayName = "Ultima Nexus - Checkstyle"
                description = "Checkstyle code style verification"
                tags = pluginTags
            }
            named("dev.daymor.ultimanexus.jvm.gradle.check.pmd") {
                displayName = "Ultima Nexus - PMD"
                description = "PMD static code analysis"
                tags = pluginTags
            }
            named("dev.daymor.ultimanexus.jvm.gradle.check.spotbugs") {
                displayName = "Ultima Nexus - SpotBugs"
                description = "SpotBugs bug pattern detection"
                tags = pluginTags
            }
            named("dev.daymor.ultimanexus.jvm.gradle.check.format-java") {
                displayName = "Ultima Nexus - Java Formatting"
                description = "Java code formatting with Spotless"
                tags = pluginTags
            }
            named("dev.daymor.ultimanexus.jvm.gradle.check.format-gradle") {
                displayName = "Ultima Nexus - Gradle Formatting"
                description = "Gradle script formatting with Spotless"
                tags = pluginTags
            }
            named("dev.daymor.ultimanexus.jvm.gradle.check.format-gradle.root") {
                displayName = "Ultima Nexus - Root Gradle Formatting"
                description = "Root project Gradle script formatting"
                tags = pluginTags
            }
            named("dev.daymor.ultimanexus.jvm.gradle.check.spotless-base") {
                displayName = "Ultima Nexus - Spotless Base"
                description = "Base Spotless configuration for code formatting"
                tags = pluginTags
            }
            named("dev.daymor.ultimanexus.jvm.gradle.check.dependencies") {
                displayName = "Ultima Nexus - Dependency Analysis"
                description = "Dependency analysis and classpath collision detection"
                tags = pluginTags
            }
            named("dev.daymor.ultimanexus.jvm.gradle.check.dependencies.root") {
                displayName = "Ultima Nexus - Root Dependency Analysis"
                description = "Root project dependency analysis"
                tags = pluginTags
            }

            // Dependency Plugins
            named("dev.daymor.ultimanexus.jvm.gradle.dependency.jspecify") {
                displayName = "Ultima Nexus - JSpecify"
                description = "JSpecify nullability annotations"
                tags = pluginTags
            }
            named("dev.daymor.ultimanexus.jvm.gradle.dependency.lombok") {
                displayName = "Ultima Nexus - Lombok"
                description = "Lombok annotation processor for main sources"
                tags = pluginTags
            }
            named("dev.daymor.ultimanexus.jvm.gradle.dependency.lombok-test") {
                displayName = "Ultima Nexus - Lombok Test"
                description = "Lombok annotation processor for test sources"
                tags = pluginTags
            }
            named("dev.daymor.ultimanexus.jvm.gradle.dependency.spring-boot-application") {
                displayName = "Ultima Nexus - Spring Boot App Dependencies"
                description = "Spring Boot application runtime dependencies"
                tags = pluginTags
            }
            named("dev.daymor.ultimanexus.jvm.gradle.dependency.spring-boot-development") {
                displayName = "Ultima Nexus - Spring Boot Dev Dependencies"
                description = "Spring Boot development-time dependencies"
                tags = pluginTags
            }
            named("dev.daymor.ultimanexus.jvm.gradle.dependency.spring-boot-test") {
                displayName = "Ultima Nexus - Spring Boot Test Dependencies"
                description = "Spring Boot test dependencies"
                tags = pluginTags
            }

            // Feature Plugins
            named("dev.daymor.ultimanexus.jvm.gradle.feature.compile-java") {
                displayName = "Ultima Nexus - Java Compilation"
                description = "Java compilation with toolchain support"
                tags = pluginTags
            }
            named("dev.daymor.ultimanexus.jvm.gradle.feature.javadoc") {
                displayName = "Ultima Nexus - Javadoc"
                description = "Javadoc generation configuration"
                tags = pluginTags
            }
            named("dev.daymor.ultimanexus.jvm.gradle.feature.publish") {
                displayName = "Ultima Nexus - Publishing"
                description = "Maven Central publishing with signing"
                tags = pluginTags
            }
            named("dev.daymor.ultimanexus.jvm.gradle.feature.publish-java") {
                displayName = "Ultima Nexus - Java Publishing"
                description = "Java library publishing with sources and javadoc"
                tags = pluginTags
            }
            named("dev.daymor.ultimanexus.jvm.gradle.feature.spring-boot") {
                displayName = "Ultima Nexus - Spring Boot"
                description = "Spring Boot library dependencies"
                tags = pluginTags
            }
            named("dev.daymor.ultimanexus.jvm.gradle.feature.spring-boot-application") {
                displayName = "Ultima Nexus - Spring Boot App Feature"
                description = "Spring Boot application configuration"
                tags = pluginTags
            }
            named("dev.daymor.ultimanexus.jvm.gradle.feature.version-platform") {
                displayName = "Ultima Nexus - Version Platform"
                description = "Java Platform for centralized version management"
                tags = pluginTags
            }
            named("dev.daymor.ultimanexus.jvm.gradle.feature.aggregation") {
                displayName = "Ultima Nexus - Aggregation"
                description = "Multi-project aggregation for reporting"
                tags = pluginTags
            }
            named("dev.daymor.ultimanexus.jvm.gradle.feature.use-all-catalog-versions") {
                displayName = "Ultima Nexus - Catalog Versions"
                description = "Apply all version catalog constraints"
                tags = pluginTags
            }
            named("dev.daymor.ultimanexus.jvm.gradle.feature.antora") {
                displayName = "Ultima Nexus - Antora"
                description = "Antora documentation site generation"
                tags = pluginTags
            }
            named("dev.daymor.ultimanexus.jvm.gradle.feature.antora-ui") {
                displayName = "Ultima Nexus - Antora UI"
                description = "Antora UI bundle generation"
                tags = pluginTags
            }
            named("dev.daymor.ultimanexus.jvm.gradle.feature.git-hooks") {
                displayName = "Ultima Nexus - Git Hooks"
                description = "Git pre-commit hooks for quality checks"
                tags = pluginTags
            }
            named("dev.daymor.ultimanexus.jvm.gradle.feature.project-structure") {
                displayName = "Ultima Nexus - Project Structure"
                description = "Auto-discover subprojects in multi-project builds"
                tags = pluginTags
            }
            named("dev.daymor.ultimanexus.jvm.gradle.feature.repositories") {
                displayName = "Ultima Nexus - Repositories"
                description = "Configures Maven Central repository for dependency resolution"
                tags = pluginTags
            }

            // Report Plugins
            named("dev.daymor.ultimanexus.jvm.gradle.report.code-coverage") {
                displayName = "Ultima Nexus - Code Coverage"
                description = "JaCoCo code coverage aggregation"
                tags = pluginTags
            }
            named("dev.daymor.ultimanexus.jvm.gradle.report.test") {
                displayName = "Ultima Nexus - Test Report"
                description = "Aggregated test results report"
                tags = pluginTags
            }
            named("dev.daymor.ultimanexus.jvm.gradle.report.sbom") {
                displayName = "Ultima Nexus - SBOM"
                description = "Software Bill of Materials (CycloneDX)"
                tags = pluginTags
            }
            named("dev.daymor.ultimanexus.jvm.gradle.report.plugin-analysis") {
                displayName = "Ultima Nexus - Plugin Analysis"
                description = "Plugin application order diagram"
                tags = pluginTags
            }

            // Test Plugins
            named("dev.daymor.ultimanexus.jvm.gradle.test.test") {
                displayName = "Ultima Nexus - Unit Tests"
                description = "JUnit 5 unit testing with JaCoCo coverage"
                tags = pluginTags
            }
            named("dev.daymor.ultimanexus.jvm.gradle.test.integration-test") {
                displayName = "Ultima Nexus - Integration Tests"
                description = "Integration test source set and tasks"
                tags = pluginTags
            }
            named("dev.daymor.ultimanexus.jvm.gradle.test.functional-test") {
                displayName = "Ultima Nexus - Functional Tests"
                description = "Functional test source set and tasks"
                tags = pluginTags
            }
            named("dev.daymor.ultimanexus.jvm.gradle.test.performance-test") {
                displayName = "Ultima Nexus - Performance Tests"
                description = "Performance test source set and tasks"
                tags = pluginTags
            }
            named("dev.daymor.ultimanexus.jvm.gradle.test.test-fixtures") {
                displayName = "Ultima Nexus - Test Fixtures"
                description = "Shared test fixtures library"
                tags = pluginTags
            }
        }
    }
}
