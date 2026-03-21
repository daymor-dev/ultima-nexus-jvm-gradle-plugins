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
import dev.daymor.ultimanexus.jvm.gradle.util.PropertyUtils.conventionFromProperty

/**
 * Plugin: dev.daymor.ultimanexus.jvm.gradle.feature.graalvm-native
 *
 * Feature plugin for GraalVM Native Image support.
 * Applies the org.graalvm.buildtools.native plugin and configures
 * default native image build settings.
 *
 * Features:
 * - GraalVM Native Image compilation (nativeCompile task)
 * - Native test execution (nativeTest task)
 * - Configurable verbose, quickBuild, fallback, and richOutput options
 * - Supports configuration via extension DSL or gradle.properties
 *
 * Extension configuration:
 * ```kotlin
 * graalvmNativeConfig {
 *     verbose.set(false)
 *     quickBuild.set(false)
 *     fallback.set(false)
 *     richOutput.set(true)
 * }
 * ```
 *
 * Or via gradle.properties:
 * ```properties
 * graalvmNative.verbose=false
 * graalvmNative.quickBuild=false
 * graalvmNative.fallback=false
 * graalvmNative.richOutput=true
 * ```
 */
plugins {
    java
    id("org.graalvm.buildtools.native")
}

interface GraalvmNativeConfigExtension {
    val verbose: Property<Boolean>
    val quickBuild: Property<Boolean>
    val fallback: Property<Boolean>
    val richOutput: Property<Boolean>
}

val graalvmNativeConfig =
    extensions.create<GraalvmNativeConfigExtension>("graalvmNativeConfig")

graalvmNativeConfig.verbose.conventionFromProperty(
    project, PropertyKeys.GraalvmNative.VERBOSE, false
)
graalvmNativeConfig.quickBuild.conventionFromProperty(
    project, PropertyKeys.GraalvmNative.QUICK_BUILD, false
)
graalvmNativeConfig.fallback.conventionFromProperty(
    project, PropertyKeys.GraalvmNative.FALLBACK, false
)
graalvmNativeConfig.richOutput.conventionFromProperty(
    project, PropertyKeys.GraalvmNative.RICH_OUTPUT, true
)

graalvmNative {
    binaries {
        named("main") {
            verbose.set(graalvmNativeConfig.verbose)
            quickBuild.set(graalvmNativeConfig.quickBuild)
            fallback.set(graalvmNativeConfig.fallback)
            richOutput.set(graalvmNativeConfig.richOutput)
        }
    }
}
