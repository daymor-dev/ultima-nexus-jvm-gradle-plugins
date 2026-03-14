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
import dev.daymor.ultimanexus.jvm.gradle.util.PropertyUtils.findPropertyOrNull
import org.gradle.api.Project
import org.gradle.api.plugins.jvm.JvmTestSuite
import org.gradle.testing.base.TestingExtension

object ReportUtils {

    fun Project.ensureTestSuitesRegistered() {
        val suiteNames = findPropertyOrNull(PropertyKeys.Test.SUITES)
            ?.split(",")
            ?.map { it.trim() }
            ?.filter { it.isNotBlank() }
            ?: Defaults.DEFAULT_TEST_SUITES
        val testing = extensions.getByType(TestingExtension::class.java)
        suiteNames.forEach { suiteName ->
            if (testing.suites.findByName(suiteName) == null) {
                testing.suites.register(suiteName, JvmTestSuite::class.java)
            }
        }
    }

    fun Project.isJacocoEnabledForSuite(suiteName: String): Boolean {
        val globalDefault = findPropertyOrNull(PropertyKeys.Test.USE_JACOCO)
            ?.toBoolean() ?: true
        val suiteDefault = if (suiteName in Defaults.SUITES_WITHOUT_JACOCO) false
            else globalDefault
        return findPropertyOrNull(
            "${PropertyKeys.Test.SUITE_PREFIX}$suiteName.useJacoco"
        )?.toBoolean() ?: suiteDefault
    }
}
