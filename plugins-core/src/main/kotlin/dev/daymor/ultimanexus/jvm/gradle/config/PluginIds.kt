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

package dev.daymor.ultimanexus.jvm.gradle.config

object PluginIds {
    const val PREFIX = "dev.daymor.ultimanexus.jvm.gradle"

    object Base {
        const val LIFECYCLE = "$PREFIX.base.lifecycle"
        const val LIFECYCLE_ROOT = "$PREFIX.base.lifecycle.root"
        const val IDENTITY = "$PREFIX.base.identity"
        const val DEPENDENCY_RULES = "$PREFIX.base.dependency-rules"
        const val REPOSITORIES = "$PREFIX.base.repositories"
    }

    object Bundle {
        const val GRADLE_PROJECT = "$PREFIX.bundle.gradle-project"
        const val GRADLE_PROJECT_ROOT = "$PREFIX.bundle.gradle-project-root"
        const val CHECK = "$PREFIX.bundle.check"
        const val TEST = "$PREFIX.bundle.test"
        const val REPORT = "$PREFIX.bundle.report"
        const val JAVA_SIMPLE_APPLICATION = "$PREFIX.bundle.java-simple-application"
        const val JAVA_SIMPLE_LIBRARY = "$PREFIX.bundle.java-simple-library"
        const val JAVA_COMPLETE_APPLICATION = "$PREFIX.bundle.java-complete-application"
        const val JAVA_COMPLETE_LIBRARY = "$PREFIX.bundle.java-complete-library"
        const val JVM_COMPLETE = "$PREFIX.bundle.jvm-complete"
        const val SPRING_BOOT_SIMPLE_APPLICATION = "$PREFIX.bundle.spring-boot-simple-application"
        const val SPRING_BOOT_SIMPLE_LIBRARY = "$PREFIX.bundle.spring-boot-simple-library"
        const val SPRING_BOOT_COMPLETE_APPLICATION = "$PREFIX.bundle.spring-boot-complete-application"
        const val SPRING_BOOT_COMPLETE_LIBRARY = "$PREFIX.bundle.spring-boot-complete-library"
        const val JSPECIFY = "$PREFIX.bundle.jspecify"
        const val LOMBOK = "$PREFIX.bundle.lombok"
        const val DOCUMENTATION = "$PREFIX.bundle.documentation"
        const val PLATFORM = "$PREFIX.bundle.platform"
        const val ULTIMA_NEXUS_JVM_SETTINGS = "$PREFIX.bundle.ultima-nexus-jvm-settings"
        const val ULTIMA_NEXUS_JVM_APPLICATION = "$PREFIX.bundle.ultima-nexus-jvm-application"
        const val ULTIMA_NEXUS_JVM_LIBRARY = "$PREFIX.bundle.ultima-nexus-jvm-library"
        const val ULTIMA_NEXUS_JVM_COMPLETE = "$PREFIX.bundle.ultima-nexus-jvm-complete"
    }

    object Feature {
        const val COMPILE_JAVA = "$PREFIX.feature.compile-java"
        const val JAVA_APPLICATION = "$PREFIX.feature.java-application"
        const val JAVA_LIBRARY = "$PREFIX.feature.java-library"
        const val JAVADOC = "$PREFIX.feature.javadoc"
        const val SPRING_BOOT_APPLICATION = "$PREFIX.feature.spring-boot-application"
        const val SPRING_BOOT_LIBRARY = "$PREFIX.feature.spring-boot-library"
        const val PUBLISH = "$PREFIX.feature.publish"
        const val PUBLISH_JAVA = "$PREFIX.feature.publish-java"
        const val PUBLISH_MAVEN_CENTRAL = "$PREFIX.feature.publish-maven-central"
        const val PUBLISH_MAVEN_CENTRAL_AGGREGATION = "$PREFIX.feature.publish-maven-central-aggregation"
        const val ANTORA = "$PREFIX.feature.antora"
        const val ANTORA_UI = "$PREFIX.feature.antora-ui"
        const val PROJECT_STRUCTURE = "$PREFIX.feature.project-structure"
        const val GIT_HOOKS = "$PREFIX.feature.git-hooks"
        const val BUILD_CACHE = "$PREFIX.feature.build-cache"
        const val VERSION_PLATFORM = "$PREFIX.feature.version-platform"
        const val USE_ALL_CATALOG_VERSIONS = "$PREFIX.feature.use-all-catalog-versions"
        const val AGGREGATION = "$PREFIX.feature.aggregation"
        const val COMPOSITE_BUILD = "$PREFIX.feature.composite-build"
        const val GRADLE_PLUGIN_PUBLISH = "$PREFIX.feature.gradle-plugin-publish"
    }

    object Check {
        const val CHECKSTYLE = "$PREFIX.check.checkstyle"
        const val PMD = "$PREFIX.check.pmd"
        const val SPOTBUGS = "$PREFIX.check.spotbugs"
        const val SPOTLESS_BASE = "$PREFIX.check.spotless-base"
        const val FORMAT_JAVA = "$PREFIX.check.format-java"
        const val FORMAT_GRADLE = "$PREFIX.check.format-gradle"
        const val FORMAT_GRADLE_ROOT = "$PREFIX.check.format-gradle.root"
        const val DEPENDENCIES = "$PREFIX.check.dependencies"
        const val DEPENDENCIES_ROOT = "$PREFIX.check.dependencies.root"
        const val DEPENDENCY_VERSIONS = "$PREFIX.check.dependency-versions"
    }

    object Dependency {
        const val JSPECIFY = "$PREFIX.dependency.jspecify"
        const val JSPECIFY_TEST = "$PREFIX.dependency.jspecify-test"
        const val LOMBOK = "$PREFIX.dependency.lombok"
        const val LOMBOK_TEST = "$PREFIX.dependency.lombok-test"
        const val SPRING_BOOT_TEST = "$PREFIX.dependency.spring-boot-test"
    }

    object Report {
        const val CODE_COVERAGE = "$PREFIX.report.code-coverage"
        const val TEST = "$PREFIX.report.test"
        const val SBOM = "$PREFIX.report.sbom"
        const val PLUGIN_ANALYSIS = "$PREFIX.report.plugin-analysis"
        const val DEVELOCITY = "$PREFIX.report.develocity"
    }

    object Test {
        const val GRADLE_PLUGIN = "$PREFIX.test.gradle-plugin"
        const val UNIT = "$PREFIX.test.unit"
        const val TEST = "$PREFIX.test.test"
        const val TEST_SUITES = "$PREFIX.test.test-suites"
        const val TEST_FIXTURES = "$PREFIX.test.test-fixtures"
    }
}
