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

import dev.daymor.ultimanexus.jvm.gradle.config.PropertyKeys

/**
 * Settings plugin for Gradle Develocity (Build Scans) integration.
 * Build Scans are FREE and provide build timeline, test analysis, and cache debugging.
 *
 * Configuration via gradle.properties:
 * ```properties
 * develocity.enabled=true                    # Enable/disable (default: true)
 * develocity.serverUrl=https://scans.gradle.com  # Server URL (default: gradle.com)
 * ```
 *
 * Behavior:
 *   - On CI: Auto-publishes build scans
 *   - Locally: Opt-in via --scan flag
 *   - Adds tags: CI/LOCAL, branch name, GitHub Actions info
 */
plugins {
    id("com.gradle.develocity")
}

val isEnabled = providers.gradleProperty(PropertyKeys.Develocity.ENABLED)
    .orElse("true")
    .map { it.toBoolean() }
    .get()

if (isEnabled) {
    val serverUrl = providers.gradleProperty(PropertyKeys.Develocity.SERVER_URL)
        .orElse("https://scans.gradle.com")
        .get()

    val isCI = providers.environmentVariable("CI").isPresent
    // Extract at settings time to avoid capturing gradle object in lambda closure
    val isBuildScanRequested = gradle.startParameter.isBuildScan

    develocity {
        server = serverUrl

        buildScan {
            termsOfUseUrl = "https://gradle.com/help/legal-terms-of-use"
            termsOfUseAgree = "yes"

            // Auto-publish on CI, opt-in locally via --scan
            publishing.onlyIf { isCI || isBuildScanRequested }

            // Add useful tags
            tag(if (isCI) "CI" else "LOCAL")

            // GitHub Actions integration
            providers.environmentVariable("GITHUB_REF_NAME").orNull?.let { tag(it) }
            providers.environmentVariable("GITHUB_REPOSITORY").orNull?.let { value("repository", it) }
            providers.environmentVariable("GITHUB_SHA").orNull?.let { value("commit", it.take(7)) }
            providers.environmentVariable("GITHUB_RUN_ID").orNull?.let { value("run", it) }

            // GitLab CI integration
            providers.environmentVariable("CI_COMMIT_REF_NAME").orNull?.let { tag(it) }
            providers.environmentVariable("CI_PROJECT_PATH").orNull?.let { value("repository", it) }
            providers.environmentVariable("CI_COMMIT_SHORT_SHA").orNull?.let { value("commit", it) }
        }
    }
}
