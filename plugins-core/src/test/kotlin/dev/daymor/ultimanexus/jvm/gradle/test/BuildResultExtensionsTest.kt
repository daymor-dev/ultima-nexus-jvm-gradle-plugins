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

package dev.daymor.ultimanexus.jvm.gradle.test

import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.BuildTask
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class BuildResultExtensionsTest {

    @Nested
    inner class TaskSucceeded {

        @Test
        fun `returns true when task outcome is SUCCESS`() {
            val buildResult = mockBuildResult(":test", TaskOutcome.SUCCESS)

            assertThat(buildResult.taskSucceeded(":test")).isTrue()
        }

        @Test
        fun `returns false when task outcome is FAILED`() {
            val buildResult = mockBuildResult(":test", TaskOutcome.FAILED)

            assertThat(buildResult.taskSucceeded(":test")).isFalse()
        }

        @Test
        fun `returns false when task does not exist`() {
            val buildResult = mockBuildResult(":other", TaskOutcome.SUCCESS)

            assertThat(buildResult.taskSucceeded(":missing")).isFalse()
        }
    }

    @Nested
    inner class TaskFailed {

        @Test
        fun `returns true when task outcome is FAILED`() {
            val buildResult = mockBuildResult(":test", TaskOutcome.FAILED)

            assertThat(buildResult.taskFailed(":test")).isTrue()
        }

        @Test
        fun `returns false when task outcome is SUCCESS`() {
            val buildResult = mockBuildResult(":test", TaskOutcome.SUCCESS)

            assertThat(buildResult.taskFailed(":test")).isFalse()
        }

        @Test
        fun `returns false when task does not exist`() {
            val buildResult = mockBuildResult(":other", TaskOutcome.FAILED)

            assertThat(buildResult.taskFailed(":missing")).isFalse()
        }
    }

    @Nested
    inner class TaskSkipped {

        @Test
        fun `returns true when task outcome is SKIPPED`() {
            val buildResult = mockBuildResult(":test", TaskOutcome.SKIPPED)

            assertThat(buildResult.taskSkipped(":test")).isTrue()
        }

        @Test
        fun `returns false when task outcome is SUCCESS`() {
            val buildResult = mockBuildResult(":test", TaskOutcome.SUCCESS)

            assertThat(buildResult.taskSkipped(":test")).isFalse()
        }
    }

    @Nested
    inner class TaskUpToDate {

        @Test
        fun `returns true when task outcome is UP_TO_DATE`() {
            val buildResult = mockBuildResult(":test", TaskOutcome.UP_TO_DATE)

            assertThat(buildResult.taskUpToDate(":test")).isTrue()
        }

        @Test
        fun `returns false when task outcome is SUCCESS`() {
            val buildResult = mockBuildResult(":test", TaskOutcome.SUCCESS)

            assertThat(buildResult.taskUpToDate(":test")).isFalse()
        }
    }

    @Nested
    inner class HasTask {

        @Test
        fun `returns true when task exists`() {
            val buildResult = mockBuildResult(":test", TaskOutcome.SUCCESS)

            assertThat(buildResult.hasTask(":test")).isTrue()
        }

        @Test
        fun `returns false when task does not exist`() {
            val buildResult = mockBuildResult(":other", TaskOutcome.SUCCESS)

            assertThat(buildResult.hasTask(":missing")).isFalse()
        }
    }

    @Nested
    inner class OutputContains {

        @Test
        fun `returns true when output contains text`() {
            val buildResult = mockk<BuildResult>()
            every { buildResult.output } returns "BUILD SUCCESSFUL in 5s"

            assertThat(buildResult.outputContains("SUCCESSFUL")).isTrue()
        }

        @Test
        fun `returns false when output does not contain text`() {
            val buildResult = mockk<BuildResult>()
            every { buildResult.output } returns "BUILD FAILED"

            assertThat(buildResult.outputContains("SUCCESSFUL")).isFalse()
        }

        @Test
        fun `is case sensitive`() {
            val buildResult = mockk<BuildResult>()
            every { buildResult.output } returns "BUILD SUCCESSFUL"

            assertThat(buildResult.outputContains("successful")).isFalse()
        }
    }

    private fun mockBuildResult(taskPath: String, outcome: TaskOutcome): BuildResult {
        val buildTask = mockk<BuildTask>()
        every { buildTask.outcome } returns outcome

        val buildResult = mockk<BuildResult>()
        every { buildResult.task(taskPath) } returns buildTask
        every { buildResult.task(neq(taskPath)) } returns null

        return buildResult
    }
}
