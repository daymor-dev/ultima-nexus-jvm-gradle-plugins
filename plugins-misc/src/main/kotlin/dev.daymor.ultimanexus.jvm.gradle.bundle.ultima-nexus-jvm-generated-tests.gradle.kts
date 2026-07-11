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

import dev.daymor.ultimanexus.jvm.gradle.config.Defaults.DependencyScope
import dev.daymor.ultimanexus.jvm.gradle.config.Defaults.UltimaNexusJvm.CatalogLibrary
import dev.daymor.ultimanexus.jvm.gradle.util.DependencyUtils.Fallbacks
import dev.daymor.ultimanexus.jvm.gradle.util.DependencyUtils.getLibraryOrNull
import dev.daymor.ultimanexus.jvm.gradle.util.DependencyUtils.getLibsCatalogOrNull

/**
 * Plugin: dev.daymor.ultimanexus.jvm.gradle.bundle.ultima-nexus-jvm-generated-tests
 *
 * Wires the four test tiers the Ultima Nexus `@GenerateTests` annotation processor emits.
 *
 * The processor writes every generated test class into the MAIN annotation-processor output,
 * tagged by tier (`generated-unit`, `generated-integration`, `generated-functional`,
 * `generated-performance`). This plugin points each test task at that output and filters it
 * to the tier the task owns, so the generated suites run exactly like hand-written ones:
 *
 *   ./gradlew test              -> generated *Test  (unit)
 *   ./gradlew integrationTest   -> generated *IT
 *   ./gradlew functionalTest    -> generated *FT
 *   ./gradlew performanceTest   -> generated *PT  (JMH)
 *   ./gradlew allTest           -> all four
 *
 * It also adds the test-side dependencies the generated classes compile and run against
 * (the test-base bundle, the per-tier starters, and the JMH annotation processor the
 * generated benchmarks need).
 *
 * This is a framework bundle, not a generic feature: it only makes sense for a project whose
 * sources carry Ultima Nexus `@GenerateTests` annotations. Apply it alongside
 * `bundle.ultima-nexus-jvm-application`.
 *
 * Override any dependency via version catalog entries:
 *   ultima-nexus-jvm-test-processor            -> annotationProcessor
 *   ultima-nexus-jvm-starter-test-base         -> compileOnly + per-tier runtimeOnly
 *   ultima-nexus-jvm-starter-integration-test  -> integrationTestImplementation
 *   ultima-nexus-jvm-starter-functional-test   -> functionalTestImplementation
 *   ultima-nexus-jvm-starter-performance-test  -> performanceTestImplementation
 *   jmh-generator-annprocess                   -> performanceTestAnnotationProcessor
 *
 * Usage:
 * ```kotlin
 * plugins {
 *     id("dev.daymor.ultimanexus.jvm.gradle.bundle.ultima-nexus-jvm-application")
 *     id("dev.daymor.ultimanexus.jvm.gradle.bundle.ultima-nexus-jvm-generated-tests")
 * }
 * ```
 */
plugins {
    java
}

private val libs = getLibsCatalogOrNull(project)

private fun dependency(catalogKey: String, fallback: String): Any =
    libs?.let { getLibraryOrNull(it, catalogKey) } ?: fallback

private val testProcessor =
    dependency(CatalogLibrary.TEST_PROCESSOR, Fallbacks.ULTIMA_NEXUS_JVM_PROCESSOR_SPRING_TEST)
private val testBase =
    dependency(CatalogLibrary.STARTER_TEST_BASE, Fallbacks.ULTIMA_NEXUS_JVM_STARTER_TEST_BASE)
private val performanceTestStarter =
    dependency(CatalogLibrary.STARTER_PERFORMANCE_TEST, Fallbacks.ULTIMA_NEXUS_JVM_STARTER_PERFORMANCE_TEST)

dependencies {
    add(DependencyScope.COMPILE_ONLY, testBase)
    add(DependencyScope.COMPILE_ONLY, performanceTestStarter)
    add(DependencyScope.ANNOTATION_PROCESSOR, testProcessor)

    add("testRuntimeOnly", testBase)

    add("integrationTestImplementation", testBase)
    add(
        "integrationTestImplementation",
        dependency(CatalogLibrary.STARTER_INTEGRATION_TEST, Fallbacks.ULTIMA_NEXUS_JVM_STARTER_INTEGRATION_TEST)
    )

    add("functionalTestRuntimeOnly", testBase)
    add(
        "functionalTestImplementation",
        dependency(CatalogLibrary.STARTER_FUNCTIONAL_TEST, Fallbacks.ULTIMA_NEXUS_JVM_STARTER_FUNCTIONAL_TEST)
    )

    add("performanceTestRuntimeOnly", testBase)
    add("performanceTestImplementation", performanceTestStarter)
    add(
        "performanceTestAnnotationProcessor",
        dependency(CatalogLibrary.JMH_GENERATOR_ANNPROCESS, Fallbacks.JMH_GENERATOR_ANNPROCESS)
    )
}

private val mainClassesDirs = sourceSets["main"].output.classesDirs

/**
 * Scans the main output for generated test classes without re-adding it to the runtime classpath.
 *
 * The classes are already reachable — a test suite resolves the project through its jar or through
 * `sourceSets.main.output`. Appending the directory a second time would put every `META-INF`
 * resource on the classpath twice, and a runtime that resolves a resource by a unique path (a
 * Liquibase changelog, a service descriptor) fails on the duplicate rather than picking one.
 */
private fun Test.scanGeneratedClasses() {
    testClassesDirs += mainClassesDirs
}

private fun wireGeneratedTier(taskName: String, pattern: String, tag: String) =
    tasks.named<Test>(taskName) {
        scanGeneratedClasses()
        include(pattern)
        useJUnitPlatform { includeTags(tag) }
    }

tasks.named<Test>("test") {
    scanGeneratedClasses()
    include("**/*Test.class")
    useJUnitPlatform { excludeTags("generated-integration", "generated-functional", "generated-performance") }
}

wireGeneratedTier("integrationTest", "**/*IT.class", "generated-integration")
wireGeneratedTier("functionalTest", "**/*FT.class", "generated-functional")

private val generatedMainSources = layout.buildDirectory.dir("generated/sources/annotationProcessor/java/main")

sourceSets.named("performanceTest") { java.srcDir(files(generatedMainSources).builtBy("compileJava")) }

// The generated *PT source references the entity and application classes, which the main output
// supplies at compile time. The runtime classpath already resolves them through the project itself.
tasks.named<JavaCompile>("compilePerformanceTestJava") {
    include("**/*PT.java")
    classpath += mainClassesDirs
}
