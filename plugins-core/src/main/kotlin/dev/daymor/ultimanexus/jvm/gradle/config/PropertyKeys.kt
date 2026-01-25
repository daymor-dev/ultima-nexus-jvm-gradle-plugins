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

object PropertyKeys {
    object Identity {
        const val ROOT_PROJECT_NAME = "rootProjectName"
        const val VERSION = "version"
        const val GROUP_ID = "groupId"
        const val DESCRIPTION = "description"
    }

    object Plugin {
        const val WEBSITE = "pluginWebsite"
        const val VCS_URL = "pluginVcsUrl"
        const val TAGS = "pluginTags"
        const val ID_PREFIX = "pluginIdPrefix"
    }

    object Build {
        const val INCLUDED_BUILDS = "includedBuilds"
        const val COMPOSITE_BUILD_PREFIX = "compositeBuildPrefix"
        const val SHARED_GRADLE_PATH = "sharedGradlePath"
        const val PROJECT_STRUCTURE_DEPTH = "projectStructureDepth"
        const val PROJECT_STRUCTURE_EXCLUSIONS = "projectStructureExclusions"
    }

    const val JDK_VERSION = "jdkVersion"
    const val CHECK_ARTIFACT_NAME = "checkArtifactName"

    object Test {
        const val MAX_HEAP_SIZE = "test.maxHeapSize"
        const val MAX_PARALLEL_FORKS = "test.maxParallelForks"
        const val SHOW_STANDARD_STREAMS = "test.showStandardStreams"
        const val FILE_ENCODING = "test.fileEncoding"
        const val USE_BYTE_BUDDY_AGENT = "test.useByteBuddyAgent"
        const val SUITES = "test.suites"
        const val SUITE_PREFIX = "test.suite."
    }

    object Checkstyle {
        const val CONFIG_FILE = "checkstyle.configFile"
        const val HEADER_FILE = "checkstyle.headerFile"
        const val SUPPRESSIONS_FILE = "checkstyle.suppressionsFile"
        const val FILE_SUPPRESSIONS_FILE = "checkstyle.fileSuppressionsFile"
    }

    object Pmd {
        const val RULE_SET_FILE = "pmd.ruleSetFile"
    }

    object SpotBugs {
        const val IGNORE_FAILURES = "spotbugs.ignoreFailures"
        const val SHOW_STACK_TRACES = "spotbugs.showStackTraces"
        const val SHOW_PROGRESS = "spotbugs.showProgress"
        const val EFFORT = "spotbugs.effort"
        const val REPORT_LEVEL = "spotbugs.reportLevel"
        const val EXCLUDE_FILTER_FILE = "spotbugs.excludeFilterFile"
    }

    object Format {
        const val GRADLE_KOTLIN_TARGET = "formatGradleRoot.kotlinGradleTarget"
        const val GRADLE_KOTLIN_VERSION = "formatGradleRoot.kotlinTarget"
        const val JAVA_FORMATTER_CONFIG = "formatJava.formatterConfigFile"
        const val JAVA_LICENSE_HEADER_FILE = "formatJava.licenseHeaderFile"
        const val JAVA_LICENSE_HEADER_TEXT = "formatJava.licenseHeaderText"
        const val IMPORT_SAME_PACKAGE_DEPTH = "formatJava.importSamePackageDepth"
        const val IMPORT_STANDARD_PACKAGE_REGEX = "formatJava.importStandardPackageRegex"
        const val IMPORT_SPECIAL_IMPORTS_REGEX = "formatJava.importSpecialImportsRegex"
        const val COMPANY = "company"
    }

    object Publish {
        const val PROJECT_URL = "publish.projectUrl"
        const val INCEPTION_YEAR = "publish.inceptionYear"
        const val LICENSE_NAME = "publish.licenseName"
        const val LICENSE_URL = "publish.licenseUrl"
        const val DEVELOPER_ID = "publish.developerId"
        const val DEVELOPER_NAME = "publish.developerName"
        const val DEVELOPER_EMAIL = "publish.developerEmail"
        const val DEVELOPER_URL = "publish.developerUrl"
        const val DEVELOPER_ORGANIZATION = "publish.developerOrganization"
        const val DEVELOPER_ORGANIZATION_URL = "publish.developerOrganizationUrl"
        const val SCM_URL = "publish.scmUrl"
        const val SCM_CONNECTION = "publish.scmConnection"
        const val SCM_DEVELOPER_CONNECTION = "publish.scmDeveloperConnection"
        const val SIGNING_KEY = "signingKey"
        const val SIGNING_PASSWORD = "signingPassword"
        const val MAVEN_CENTRAL_USERNAME = "mavenCentralUsername"
        const val MAVEN_CENTRAL_PASSWORD = "mavenCentralPassword"
        const val MODULES = "publishModules"
    }

    object DependencyRules {
        const val PLATFORM_PATH = "dependencyRules.platformPath"
        const val AGGREGATION_PATH = "dependencyRules.aggregationPath"
        const val AUTO_CONSUME_PLATFORM = "dependencyRules.autoConsumePlatform"
    }

    object CodeCoverage {
        const val EXCLUDE_PATTERNS = "codeCoverage.excludePatterns"
    }

    object VersionPlatform {
        const val INCLUDE_SPRING_BOOT_BOM = "versionPlatform.includeSpringBootBom"
        const val SPRING_BOOT_BOM_LIBRARY = "versionPlatform.springBootBomLibrary"
    }

    object Develocity {
        const val ENABLED = "develocity.enabled"
        const val SERVER_URL = "develocity.serverUrl"
    }

    object BuildCache {
        const val LOCAL_ENABLED = "buildCache.local.enabled"
        const val REMOTE_URL = "buildCache.remote.url"
        const val REMOTE_USERNAME = "buildCache.remote.username"
        const val REMOTE_PASSWORD = "buildCache.remote.password"
        const val REMOTE_PUSH = "buildCache.remote.push"
    }

    object Repositories {
        const val INCLUDE_MAVEN_CENTRAL = "repositories.includeMavenCentral"
        const val INCLUDE_MAVEN_LOCAL = "repositories.includeMavenLocal"
        const val ADDITIONAL_REPOS = "repositories.additional"
        const val REPO_PREFIX = "repositories.repo."
        const val URL_SUFFIX = ".url"
        const val USERNAME_SUFFIX = ".username"
        const val PASSWORD_SUFFIX = ".password"
        const val ALLOW_INSECURE_SUFFIX = ".allowInsecureProtocol"
    }
}
