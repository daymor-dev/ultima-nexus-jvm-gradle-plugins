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

tasks.register("buildAll") {
    group = "composite"
    description = "Build all included builds"
    dependsOn(gradle.includedBuilds.map { it.task(":build") })
}

tasks.register("testAll") {
    group = "composite"
    description = "Test all included builds"
    dependsOn(gradle.includedBuilds.map { it.task(":test") })
}

tasks.register("checkAll") {
    group = "composite"
    description = "Run checks on all included builds"
    dependsOn(gradle.includedBuilds.map { it.task(":check") })
}

tasks.register("validateAll") {
    group = "composite"
    description = "Validate plugins in all included builds"
    dependsOn(gradle.includedBuilds.map { it.task(":validatePlugins") })
}

tasks.register("cleanAll") {
    group = "composite"
    description = "Clean all included builds"
    dependsOn(gradle.includedBuilds.map { it.task(":clean") })
}

tasks.register("publishAll") {
    group = "composite"
    description = "Publish all included builds to Gradle Plugin Portal"
    dependsOn(gradle.includedBuilds.map { it.task(":publishPlugins") })
}
