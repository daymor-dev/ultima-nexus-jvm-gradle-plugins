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

package dev.daymor.ultimanexus.jvm.gradle.util

import dev.daymor.ultimanexus.jvm.gradle.util.TaskConfigUtils.addToQualityGates
import dev.daymor.ultimanexus.jvm.gradle.util.TaskConfigUtils.configureCheckTaskWithJavaPlugin
import org.assertj.core.api.Assertions.assertThat
import org.gradle.api.plugins.JavaPlugin
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class TaskConfigUtilsTest {

    @TempDir
    lateinit var tempDir: File

    @Nested
    inner class AddToQualityGates {

        @Test
        fun `adds task as dependency to qualityCheck`() {
            val project = ProjectBuilder.builder().withProjectDir(tempDir).build()
            project.tasks.register("qualityCheck")
            project.tasks.register("qualityGate")
            project.tasks.register("myCheckTask")

            project.addToQualityGates("myCheckTask")

            val qualityCheckDeps = project.tasks.named("qualityCheck").get().dependsOn
            assertThat(qualityCheckDeps.map { it.toString() }).anyMatch { it.contains("myCheckTask") }
        }

        @Test
        fun `adds task as dependency to qualityGate`() {
            val project = ProjectBuilder.builder().withProjectDir(tempDir).build()
            project.tasks.register("qualityCheck")
            project.tasks.register("qualityGate")
            project.tasks.register("myCheckTask")

            project.addToQualityGates("myCheckTask")

            val qualityGateDeps = project.tasks.named("qualityGate").get().dependsOn
            assertThat(qualityGateDeps.map { it.toString() }).anyMatch { it.contains("myCheckTask") }
        }
    }

    @Nested
    inner class ConfigureCheckTaskWithJavaPlugin {

        @Test
        fun `configures task group when java plugin is applied`() {
            val project = ProjectBuilder.builder().withProjectDir(tempDir).build()
            project.tasks.register("qualityCheck")
            project.tasks.register("qualityGate")
            project.tasks.register("checkstyleMain")

            project.configureCheckTaskWithJavaPlugin("checkstyleMain")
            project.plugins.apply(JavaPlugin::class.java)

            val task = project.tasks.named("checkstyleMain").get()
            assertThat(task.group).isEqualTo("verification")
        }

        @Test
        fun `uses custom task group when specified`() {
            val project = ProjectBuilder.builder().withProjectDir(tempDir).build()
            project.tasks.register("qualityCheck")
            project.tasks.register("qualityGate")
            project.tasks.register("myTask")

            project.configureCheckTaskWithJavaPlugin("myTask", "custom-group")
            project.plugins.apply(JavaPlugin::class.java)

            val task = project.tasks.named("myTask").get()
            assertThat(task.group).isEqualTo("custom-group")
        }

        @Test
        fun `does not configure task when java plugin is not applied`() {
            val project = ProjectBuilder.builder().withProjectDir(tempDir).build()
            project.tasks.register("qualityCheck")
            project.tasks.register("qualityGate")
            val task = project.tasks.register("myTask").get()
            val originalGroup = task.group

            project.configureCheckTaskWithJavaPlugin("myTask")

            assertThat(project.tasks.named("myTask").get().group).isEqualTo(originalGroup)
        }

        @Test
        fun `adds task to quality gates when java plugin is applied`() {
            val project = ProjectBuilder.builder().withProjectDir(tempDir).build()
            project.tasks.register("qualityCheck")
            project.tasks.register("qualityGate")
            project.tasks.register("pmdMain")

            project.configureCheckTaskWithJavaPlugin("pmdMain")
            project.plugins.apply(JavaPlugin::class.java)

            val qualityCheckDeps = project.tasks.named("qualityCheck").get().dependsOn
            assertThat(qualityCheckDeps.map { it.toString() }).anyMatch { it.contains("pmdMain") }
        }
    }
}
