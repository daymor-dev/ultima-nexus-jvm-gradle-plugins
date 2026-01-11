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

import dev.daymor.ultimanexus.jvm.gradle.config.Defaults
import dev.daymor.ultimanexus.jvm.gradle.config.PropertyKeys
import dev.daymor.ultimanexus.jvm.gradle.util.PropertyUtils.gradlePropertyAsBoolean
import dev.daymor.ultimanexus.jvm.gradle.util.PropertyUtils.gradlePropertyAsInt
import dev.daymor.ultimanexus.jvm.gradle.util.PropertyUtils.gradlePropertyOrNull
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.tasks.testing.Test

object TestSuiteUtils {

    fun getOrCreateByteBuddyAgentConfiguration(project: Project): Configuration =
        project.configurations.findByName(Defaults.ConfigurationName.BYTE_BUDDY_AGENT)
            ?: project.configurations.create(Defaults.ConfigurationName.BYTE_BUDDY_AGENT)

    fun configureTestTask(
        testTask: Test,
        byteBuddyAgentConfig: Configuration?,
        providers: ProviderFactory,
        useByteBuddy: Boolean = true
    ) {
        val defaultParallelForks = (Runtime.getRuntime().availableProcessors() / 2).coerceAtLeast(1)

        testTask.apply {
            group = Defaults.TaskGroup.VERIFICATION

            maxHeapSize = providers.gradlePropertyOrNull(PropertyKeys.Test.MAX_HEAP_SIZE)
                ?: Defaults.TEST_MAX_HEAP_SIZE

            maxParallelForks = providers.gradlePropertyAsInt(PropertyKeys.Test.MAX_PARALLEL_FORKS, defaultParallelForks)

            testLogging.showStandardStreams = providers.gradlePropertyAsBoolean(PropertyKeys.Test.SHOW_STANDARD_STREAMS, true)

            systemProperty(
                "file.encoding",
                providers.gradlePropertyOrNull(PropertyKeys.Test.FILE_ENCODING)
                    ?: Defaults.TEST_FILE_ENCODING
            )

            if (useByteBuddy && byteBuddyAgentConfig != null) {
                val useAgent = providers.gradlePropertyAsBoolean(PropertyKeys.Test.USE_BYTE_BUDDY_AGENT, true)

                if (useAgent && byteBuddyAgentConfig.files.size == 1) {
                    jvmArgs = listOf(
                        "-javaagent:${byteBuddyAgentConfig.singleFile.absolutePath}",
                        "-Xshare:off"
                    )
                }
            }
        }
    }
}
