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

val sharedPropsFile = file("../gradle/shared.properties")
val sharedProps = java.util.Properties()
if (sharedPropsFile.exists()) {
    sharedPropsFile.inputStream().use { sharedProps.load(it) }

    sharedProps.forEach { key, value ->
        if (key.toString().startsWith("org.gradle.")) {
            System.setProperty(key.toString(), value.toString())
        }
    }
}

gradle.beforeProject {
    sharedProps.forEach { key, value ->
        if (!key.toString().startsWith("org.gradle.") && !project.hasProperty(key.toString())) {
            project.extensions.extraProperties[key.toString()] = value.toString()
        }
    }
}

pluginManagement {
    repositories {
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}

rootProject.name = providers.gradleProperty("rootProjectName").get()
