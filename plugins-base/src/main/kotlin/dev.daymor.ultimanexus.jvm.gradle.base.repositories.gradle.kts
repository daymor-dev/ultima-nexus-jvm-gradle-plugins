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

import dev.daymor.ultimanexus.jvm.gradle.config.Defaults
import dev.daymor.ultimanexus.jvm.gradle.config.PropertyKeys
import dev.daymor.ultimanexus.jvm.gradle.util.PropertyUtils.findPropertyAsBoolean
import dev.daymor.ultimanexus.jvm.gradle.util.PropertyUtils.findPropertyOrNull

/**
 * Convention plugin for configurable repository management.
 *
 * Configures Maven Central by default with support for additional
 * public or private repositories.
 *
 * Extension configuration (build.gradle.kts):
 *   repositoriesConfig {
 *       includeMavenCentral.set(true)  // default
 *       includeMavenLocal.set(false)   // default
 *
 *       repository("jitpack") {
 *           url.set("https://jitpack.io")
 *       }
 *
 *       repository("company-nexus") {
 *           url.set("https://nexus.company.com/repository/maven-releases/")
 *           username.set(providers.gradleProperty("nexusUsername").orNull)
 *           password.set(providers.gradleProperty("nexusPassword").orNull)
 *       }
 *   }
 *
 * Or via gradle.properties:
 *   repositories.includeMavenCentral=true
 *   repositories.additional=jitpack,company-nexus
 *   repositories.repo.jitpack.url=https://jitpack.io
 *   repositories.repo.company-nexus.url=https://nexus.company.com/repository/maven-releases/
 *   repositories.repo.company-nexus.username=myuser
 *   repositories.repo.company-nexus.password=mypassword
 *
 * Environment variables (for CI/CD):
 *   REPO_COMPANY_NEXUS_USERNAME, REPO_COMPANY_NEXUS_PASSWORD
 */

interface RepositorySpec {
    val name: Property<String>
    val url: Property<String>
    val username: Property<String>
    val password: Property<String>
    val allowInsecureProtocol: Property<Boolean>
}

interface RepositoriesExtension {
    val includeMavenCentral: Property<Boolean>
    val includeMavenLocal: Property<Boolean>
    val repositories: NamedDomainObjectContainer<RepositorySpec>

    fun repository(name: String, action: Action<RepositorySpec>) {
        val repo = repositories.maybeCreate(name)
        repo.name.set(name)
        action.execute(repo)
    }
}

val repositoriesConfig = extensions.create<RepositoriesExtension>("repositoriesConfig")

repositoriesConfig.includeMavenCentral.convention(
    project.findPropertyAsBoolean(PropertyKeys.Repositories.INCLUDE_MAVEN_CENTRAL, Defaults.Repositories.INCLUDE_MAVEN_CENTRAL)
)
repositoriesConfig.includeMavenLocal.convention(
    project.findPropertyAsBoolean(PropertyKeys.Repositories.INCLUDE_MAVEN_LOCAL, Defaults.Repositories.INCLUDE_MAVEN_LOCAL)
)

fun getEnvCredential(repoName: String, suffix: String): String? {
    val envName = "REPO_${repoName.uppercase().replace("-", "_").replace(".", "_")}_$suffix"
    return providers.environmentVariable(envName).orNull
}

fun getRepoProperty(repoName: String, suffix: String): String? =
    project.findPropertyOrNull("${PropertyKeys.Repositories.REPO_PREFIX}$repoName$suffix")

val additionalReposFromProps = project.findPropertyOrNull(PropertyKeys.Repositories.ADDITIONAL_REPOS)
    ?.split(",")
    ?.map { it.trim() }
    ?.filter { it.isNotBlank() }
    ?: emptyList()

additionalReposFromProps.forEach { repoName ->
    val url = getRepoProperty(repoName, PropertyKeys.Repositories.URL_SUFFIX)
    if (url != null) {
        repositoriesConfig.repository(repoName) {
            this.url.set(url)

            val repoUsername = getEnvCredential(repoName, "USERNAME")
                ?: getRepoProperty(repoName, PropertyKeys.Repositories.USERNAME_SUFFIX)
            val repoPassword = getEnvCredential(repoName, "PASSWORD")
                ?: getRepoProperty(repoName, PropertyKeys.Repositories.PASSWORD_SUFFIX)

            repoUsername?.let { this.username.set(it) }
            repoPassword?.let { this.password.set(it) }

            val allowInsecure = getRepoProperty(repoName, PropertyKeys.Repositories.ALLOW_INSECURE_SUFFIX)
                ?.toBoolean() ?: Defaults.Repositories.ALLOW_INSECURE_PROTOCOL
            this.allowInsecureProtocol.set(allowInsecure)
        }
    }
}

repositories {
    if (repositoriesConfig.includeMavenCentral.get()) {
        mavenCentral()
    }

    if (repositoriesConfig.includeMavenLocal.get()) {
        mavenLocal()
    }

    repositoriesConfig.repositories.forEach { repoSpec ->
        val repoUrl = repoSpec.url.orNull
        if (repoUrl.isNullOrBlank()) {
            logger.warn("Repository '${repoSpec.name.orNull}' has no URL configured, skipping")
            return@forEach
        }

        maven {
            name = repoSpec.name.getOrElse("unnamed")
            url = uri(repoUrl)

            if (repoSpec.allowInsecureProtocol.getOrElse(false)) {
                isAllowInsecureProtocol = true
            }

            val repoUsername = repoSpec.username.orNull
            val repoPassword = repoSpec.password.orNull

            if (repoUsername != null && repoPassword != null) {
                credentials {
                    username = repoUsername
                    password = repoPassword
                }
            } else if (repoUsername != null || repoPassword != null) {
                logger.warn(
                    "Repository '${repoSpec.name.orNull}': Both username and password must be " +
                        "provided for authentication. Credentials will not be applied."
                )
            }
        }
    }

    if (!repositoriesConfig.includeMavenCentral.get() &&
        !repositoriesConfig.includeMavenLocal.get() &&
        repositoriesConfig.repositories.isEmpty()
    ) {
        logger.warn(
            "No repositories configured! Maven Central is disabled and no additional " +
                "repositories are defined. Dependency resolution will fail."
        )
    }
}
