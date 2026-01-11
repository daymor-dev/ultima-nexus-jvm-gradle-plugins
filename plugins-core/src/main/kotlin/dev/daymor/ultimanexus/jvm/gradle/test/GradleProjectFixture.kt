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

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import java.io.File

class GradleProjectFixture(private val projectDir: File) {

    fun withSettings(content: String): GradleProjectFixture {
        File(projectDir, "settings.gradle.kts").writeText(content)
        return this
    }

    fun withBuildScript(content: String): GradleProjectFixture {
        File(projectDir, "build.gradle.kts").writeText(content)
        return this
    }

    fun withProperties(vararg properties: Pair<String, String>): GradleProjectFixture {
        val content = properties.joinToString("\n") { "${it.first}=${it.second}" }
        File(projectDir, "gradle.properties").writeText(content)
        return this
    }

    fun withVersionCatalog(content: String): GradleProjectFixture {
        File(projectDir, "gradle").mkdirs()
        File(projectDir, "gradle/libs.versions.toml").writeText(content)
        return this
    }

    fun withSubproject(name: String, buildContent: String): GradleProjectFixture {
        val subDir = File(projectDir, name)
        subDir.mkdirs()
        File(subDir, "build.gradle.kts").writeText(buildContent)
        return this
    }

    fun withSourceFile(path: String, content: String): GradleProjectFixture {
        val file = File(projectDir, path)
        file.parentFile?.mkdirs()
        file.writeText(content)
        return this
    }

    fun runner(vararg tasks: String): GradleRunner =
        GradleRunner.create()
            .withProjectDir(projectDir)
            .withPluginClasspath()
            .withArguments(*tasks, "--stacktrace")
            .forwardOutput()

    fun build(vararg tasks: String): BuildResult = runner(*tasks).build()

    fun buildAndFail(vararg tasks: String): BuildResult = runner(*tasks).buildAndFail()

    fun help(): BuildResult = build("help")

    fun tasks(): BuildResult = build("tasks")
}
