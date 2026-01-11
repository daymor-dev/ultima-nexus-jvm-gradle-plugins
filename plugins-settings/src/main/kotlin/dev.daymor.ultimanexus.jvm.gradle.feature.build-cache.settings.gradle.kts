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
 * Settings plugin for Gradle Build Cache configuration.
 * Provides plug-and-play remote cache support for CI/CD environments.
 *
 * NOTE: This plugin configures the buildCache DSL only.
 * You must still set org.gradle.caching=true in gradle.properties to enable caching.
 *
 * Configuration via gradle.properties:
 * ```properties
 * # Enable caching (REQUIRED - cannot be set by plugin)
 * org.gradle.caching=true
 *
 * # Local cache (enabled by default for local builds)
 * buildCache.local.enabled=true
 *
 * # Remote cache (optional - for CI)
 * buildCache.remote.url=https://cache.example.com/cache/
 * buildCache.remote.username=<username>
 * buildCache.remote.password=<password>
 * buildCache.remote.push=true    # Only CI should push
 * ```
 *
 * Environment variable overrides:
 *   GRADLE_CACHE_REMOTE_URL, GRADLE_CACHE_USERNAME, GRADLE_CACHE_PASSWORD
 *
 * Behavior:
 *   - Local: Uses local cache by default (disabled on CI if remote configured)
 *   - CI: Uses remote cache if configured, only CI pushes
 */

val isCI = providers.environmentVariable("CI").isPresent

// Local cache configuration
val localEnabled = providers.gradleProperty(PropertyKeys.BuildCache.LOCAL_ENABLED)
    .orElse("true")
    .map { it.toBoolean() }
    .get()

// Remote cache configuration (properties or environment variables)
val remoteUrl = providers.environmentVariable("GRADLE_CACHE_REMOTE_URL")
    .orElse(providers.gradleProperty(PropertyKeys.BuildCache.REMOTE_URL))
    .orNull

val remoteUsername = providers.environmentVariable("GRADLE_CACHE_USERNAME")
    .orElse(providers.gradleProperty(PropertyKeys.BuildCache.REMOTE_USERNAME))
    .orNull

val remotePassword = providers.environmentVariable("GRADLE_CACHE_PASSWORD")
    .orElse(providers.gradleProperty(PropertyKeys.BuildCache.REMOTE_PASSWORD))
    .orNull

val remotePush = providers.gradleProperty(PropertyKeys.BuildCache.REMOTE_PUSH)
    .orElse(if (isCI) "true" else "false")
    .map { it.toBoolean() }
    .get()

buildCache {
    local {
        // Enable local cache, but disable on CI if remote is configured
        isEnabled = localEnabled && !(isCI && remoteUrl != null)
    }

    if (remoteUrl != null) {
        remote<HttpBuildCache> {
            url = uri(remoteUrl)
            isPush = remotePush

            if (remoteUsername != null && remotePassword != null) {
                credentials {
                    username = remoteUsername
                    password = remotePassword
                }
            }
        }
    }
}
