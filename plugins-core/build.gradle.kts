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

fun prop(key: String): String = providers.gradleProperty(key).orNull
    ?: (extra.properties[key] as? String)
    ?: error("Property '$key' not found")

plugins {
    `kotlin-dsl`
    alias(libs.plugins.gradle.plugin.publish)
}

dependencies {
    implementation(libs.gradle.plugin.publish)
    implementation(gradleTestKit())
}

group = prop("groupId")
version = prop("version")

gradlePlugin {
    website = prop("pluginWebsite")
    vcsUrl = prop("pluginVcsUrl")

    plugins {
        named(libs.plugins.ultimanexus.jvm.composite.build.get().pluginId) {
            displayName = prop("plugin.feature.composite-build.displayName")
            description = prop("plugin.feature.composite-build.description")
            tags = prop("pluginTags").split(",")
        }
        named("dev.daymor.ultimanexus.jvm.gradle.test.gradle-plugin") {
            displayName = prop("plugin.test.gradle-plugin.displayName")
            description = prop("plugin.test.gradle-plugin.description")
            tags = prop("pluginTags").split(",")
        }
        named("dev.daymor.ultimanexus.jvm.gradle.test.unit") {
            displayName = prop("plugin.test.unit.displayName")
            description = prop("plugin.test.unit.description")
            tags = prop("pluginTags").split(",")
        }
    }
}

testing.suites.named<JvmTestSuite>("test") {
    useJUnitJupiter()
    dependencies {
        implementation(libs.junit.jupiter.params)
        implementation(libs.assertj.core)
        implementation(libs.mockk)
    }
}
