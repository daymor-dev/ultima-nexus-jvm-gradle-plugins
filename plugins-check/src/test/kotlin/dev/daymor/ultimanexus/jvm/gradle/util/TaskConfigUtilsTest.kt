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
import dev.daymor.ultimanexus.jvm.gradle.util.TaskConfigUtils.configureAllCheckTasksWithJavaPlugin
import org.gradle.api.DefaultTask
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

    abstract class TestCheckTask : DefaultTask()

    @Nested
    inner class ConfigureAllCheckTasksWithJavaPlugin {

        @Test
        fun `adds all tasks of type to qualityCheck when java plugin is applied`() {
            val project = ProjectBuilder.builder().withProjectDir(tempDir).build()
            project.tasks.register("qualityCheck")
            project.tasks.register("qualityGate")
            project.tasks.register("checkMain", TestCheckTask::class.java)
            project.tasks.register("checkTest", TestCheckTask::class.java)
            project.tasks.register("checkIntegrationTest", TestCheckTask::class.java)

            project.configureAllCheckTasksWithJavaPlugin<TestCheckTask>()
            project.plugins.apply(JavaPlugin::class.java)

            val qualityCheckDeps = project.tasks.named("qualityCheck").get()
                .taskDependencies.getDependencies(null).map { it.name }
            assertThat(qualityCheckDeps).contains("checkMain", "checkTest", "checkIntegrationTest")
        }

        @Test
        fun `adds all tasks of type to qualityGate when java plugin is applied`() {
            val project = ProjectBuilder.builder().withProjectDir(tempDir).build()
            project.tasks.register("qualityCheck")
            project.tasks.register("qualityGate")
            project.tasks.register("checkMain", TestCheckTask::class.java)
            project.tasks.register("checkTest", TestCheckTask::class.java)

            project.configureAllCheckTasksWithJavaPlugin<TestCheckTask>()
            project.plugins.apply(JavaPlugin::class.java)

            val qualityGateDeps = project.tasks.named("qualityGate").get()
                .taskDependencies.getDependencies(null).map { it.name }
            assertThat(qualityGateDeps).contains("checkMain", "checkTest")
        }

        @Test
        fun `sets main task group to verification`() {
            val project = ProjectBuilder.builder().withProjectDir(tempDir).build()
            project.tasks.register("qualityCheck")
            project.tasks.register("qualityGate")
            project.tasks.register("checkMain", TestCheckTask::class.java)
            project.tasks.register("checkTest", TestCheckTask::class.java)

            project.configureAllCheckTasksWithJavaPlugin<TestCheckTask>()
            project.plugins.apply(JavaPlugin::class.java)

            assertThat(project.tasks.named("checkMain").get().group).isEqualTo("verification")
            assertThat(project.tasks.named("checkTest").get().group).isNotEqualTo("verification")
        }

        @Test
        fun `does not configure when java plugin is not applied`() {
            val project = ProjectBuilder.builder().withProjectDir(tempDir).build()
            project.tasks.register("qualityCheck")
            project.tasks.register("qualityGate")
            project.tasks.register("checkMain", TestCheckTask::class.java)

            project.configureAllCheckTasksWithJavaPlugin<TestCheckTask>()

            val qualityCheckDeps = project.tasks.named("qualityCheck").get().dependsOn
            assertThat(qualityCheckDeps).isEmpty()
        }
    }
}
