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
import dev.daymor.ultimanexus.jvm.gradle.config.UltimaNexusConfig
import dev.daymor.ultimanexus.jvm.gradle.util.PropertyUtils.conventionFromProperty
import dev.daymor.ultimanexus.jvm.gradle.util.PropertyUtils.findPropertyOrNull

/**
 * Plugin: dev.daymor.ultimanexus.jvm.gradle.feature.publish
 *
 * Configures Maven publishing with POM metadata and GPG signing.
 * Sets up publication with proper metadata for Maven Central compatibility.
 *
 * Usage:
 *   plugins {
 *       id("dev.daymor.ultimanexus.jvm.gradle.feature.publish")
 *   }
 *
 * Extension configuration (build.gradle.kts):
 *   publishConfig {
 *       projectUrl.set("https://github.com/example/project")
 *       inceptionYear.set("2025")
 *       licenseName.set("The Apache License, Version 2.0")
 *       licenseUrl.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
 *       developerId.set("developer")
 *       developerName.set("Developer Name")
 *       developerEmail.set("developer@example.com")
 *       developerUrl.set("https://example.com")
 *       developerOrganization.set("Organization")
 *       developerOrganizationUrl.set("https://organization.com")
 *       scmUrl.set("https://github.com/example/project")
 *       scmConnection.set("scm:git:git://github.com/example/project.git")
 *       scmDeveloperConnection.set("scm:git:ssh://github.com/example/project.git")
 *   }
 *
 * Gradle properties (gradle.properties):
 *   publish.project-url=https://github.com/example/project
 *   publish.inception-year=2025
 *   publish.license-name=The Apache License, Version 2.0
 *   publish.license-url=http://www.apache.org/licenses/LICENSE-2.0.txt
 *   publish.developer-id=developer
 *   publish.developer-name=Developer Name
 *   publish.developer-email=developer@example.com
 *   publish.developer-url=https://example.com
 *   publish.developer-organization=Organization
 *   publish.developer-organization-url=https://organization.com
 *   publish.scm-url=https://github.com/example/project
 *   publish.scm-connection=scm:git:git://github.com/example/project.git
 *   publish.scm-developer-connection=scm:git:ssh://github.com/example/project.git
 *
 * Signing configuration (gradle.properties or environment):
 *   publish.signing.key=<armored-private-key>
 *   publish.signing.password=<key-password>
 *   # Or environment variables:
 *   SIGNINGKEY=<armored-private-key>
 *   SIGNINGPASSWORD=<key-password>
 */

plugins {
    id("dev.daymor.ultimanexus.jvm.gradle.base.identity")
    java
    `maven-publish`
    signing
}

val ultimaNexus = UltimaNexusConfig.get(project)

interface PublishExtension {
    val projectUrl: Property<String>
    val inceptionYear: Property<String>
    val licenseName: Property<String>
    val licenseUrl: Property<String>
    val developerId: Property<String>
    val developerName: Property<String>
    val developerEmail: Property<String>
    val developerUrl: Property<String>
    val developerOrganization: Property<String>
    val developerOrganizationUrl: Property<String>
    val scmUrl: Property<String>
    val scmConnection: Property<String>
    val scmDeveloperConnection: Property<String>
}

val publishConfig = extensions.create<PublishExtension>("publishConfig")

publishConfig.projectUrl.conventionFromProperty(project, PropertyKeys.Publish.PROJECT_URL)
publishConfig.inceptionYear.conventionFromProperty(project, PropertyKeys.Publish.INCEPTION_YEAR)
publishConfig.licenseName.conventionFromProperty(project, PropertyKeys.Publish.LICENSE_NAME)
publishConfig.licenseUrl.conventionFromProperty(project, PropertyKeys.Publish.LICENSE_URL)
publishConfig.developerId.conventionFromProperty(project, PropertyKeys.Publish.DEVELOPER_ID)
publishConfig.developerName.conventionFromProperty(project, PropertyKeys.Publish.DEVELOPER_NAME)
publishConfig.developerEmail.conventionFromProperty(project, PropertyKeys.Publish.DEVELOPER_EMAIL)
publishConfig.developerUrl.conventionFromProperty(project, PropertyKeys.Publish.DEVELOPER_URL)
publishConfig.developerOrganization.conventionFromProperty(project, PropertyKeys.Publish.DEVELOPER_ORGANIZATION)
publishConfig.developerOrganizationUrl.conventionFromProperty(project, PropertyKeys.Publish.DEVELOPER_ORGANIZATION_URL)
publishConfig.scmUrl.conventionFromProperty(project, PropertyKeys.Publish.SCM_URL)
publishConfig.scmConnection.conventionFromProperty(project, PropertyKeys.Publish.SCM_CONNECTION)
publishConfig.scmDeveloperConnection.conventionFromProperty(project, PropertyKeys.Publish.SCM_DEVELOPER_CONNECTION)

publishing {
    publications.register<MavenPublication>("mavenJava") {
        from(components["java"])
        versionMapping { allVariants { fromResolutionResult() } }

        pom {
            name = project.name
            description = provider { project.description }
            inceptionYear = publishConfig.inceptionYear.getOrElse("")
            url = publishConfig.projectUrl.getOrElse("")

            licenses {
                license {
                    name = publishConfig.licenseName
                        .getOrElse("The Apache License, Version 2.0")
                    this.url = publishConfig.licenseUrl
                        .getOrElse("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    distribution = publishConfig.licenseUrl
                        .getOrElse("http://www.apache.org/licenses/LICENSE-2.0.txt")
                }
            }
            developers {
                developer {
                    id = publishConfig.developerId.getOrElse("")
                    name = publishConfig.developerName.getOrElse("")
                    email = publishConfig.developerEmail.getOrElse("")
                    this.url = publishConfig.developerUrl.getOrElse("")
                    organization = publishConfig.developerOrganization.getOrElse("")
                    organizationUrl = publishConfig.developerOrganizationUrl.getOrElse("")
                }
            }
            scm {
                this.url = publishConfig.scmUrl.getOrElse("")
                connection = publishConfig.scmConnection.getOrElse("")
                developerConnection = publishConfig.scmDeveloperConnection.getOrElse("")
            }
        }
    }
}

signing {
    useInMemoryPgpKeys(
        project.findPropertyOrNull(PropertyKeys.Publish.SIGNING_KEY)
            ?: System.getenv("SIGNINGKEY")
            ?: "",
        project.findPropertyOrNull(PropertyKeys.Publish.SIGNING_PASSWORD)
            ?: System.getenv("SIGNINGPASSWORD")
            ?: "",
    )
    sign(publishing.publications)
}

listOf(
        PublishToMavenRepository::class,
        PublishToMavenLocal::class,
        GenerateMavenPom::class,
        GenerateModuleMetadata::class,
    )
    .forEach { tasks.withType(it) { group = Defaults.TaskGroup.PUBLISHING_OTHER } }

tasks.named("publish") { group = Defaults.TaskGroup.PUBLISHING_OTHER }
