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

package dev.daymor.ultimanexus.jvm.gradle.feature

plugins {
    java
    `maven-publish`
    signing
    id("dev.daymor.ultimanexus.jvm.gradle.base.identity")
    id("com.gradleup.nmcp.aggregation")
}

/**
 * Extension for configuring publish plugin settings.
 * Allows external users to customize publishing metadata.
 */
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

// Read from gradle.properties with defaults
val projectUrlFromProps = providers.gradleProperty("publish.projectUrl").orNull
val inceptionYearFromProps = providers.gradleProperty("publish.inceptionYear").orNull
val licenseNameFromProps = providers.gradleProperty("publish.licenseName").orNull
val licenseUrlFromProps = providers.gradleProperty("publish.licenseUrl").orNull
val developerIdFromProps = providers.gradleProperty("publish.developerId").orNull
val developerNameFromProps = providers.gradleProperty("publish.developerName").orNull
val developerEmailFromProps = providers.gradleProperty("publish.developerEmail").orNull
val developerUrlFromProps = providers.gradleProperty("publish.developerUrl").orNull
val developerOrganizationFromProps = providers.gradleProperty("publish.developerOrganization").orNull
val developerOrganizationUrlFromProps = providers.gradleProperty("publish.developerOrganizationUrl").orNull
val scmUrlFromProps = providers.gradleProperty("publish.scmUrl").orNull
val scmConnectionFromProps = providers.gradleProperty("publish.scmConnection").orNull
val scmDeveloperConnectionFromProps = providers.gradleProperty("publish.scmDeveloperConnection").orNull

// Set conventions from gradle.properties
if (projectUrlFromProps != null) publishConfig.projectUrl.convention(projectUrlFromProps)
if (inceptionYearFromProps != null) publishConfig.inceptionYear.convention(inceptionYearFromProps)
if (licenseNameFromProps != null) publishConfig.licenseName.convention(licenseNameFromProps)
if (licenseUrlFromProps != null) publishConfig.licenseUrl.convention(licenseUrlFromProps)
if (developerIdFromProps != null) publishConfig.developerId.convention(developerIdFromProps)
if (developerNameFromProps != null) publishConfig.developerName.convention(developerNameFromProps)
if (developerEmailFromProps != null) publishConfig.developerEmail.convention(developerEmailFromProps)
if (developerUrlFromProps != null) publishConfig.developerUrl.convention(developerUrlFromProps)
if (developerOrganizationFromProps != null) publishConfig.developerOrganization.convention(developerOrganizationFromProps)
if (developerOrganizationUrlFromProps != null) publishConfig.developerOrganizationUrl.convention(developerOrganizationUrlFromProps)
if (scmUrlFromProps != null) publishConfig.scmUrl.convention(scmUrlFromProps)
if (scmConnectionFromProps != null) publishConfig.scmConnection.convention(scmConnectionFromProps)
if (scmDeveloperConnectionFromProps != null) publishConfig.scmDeveloperConnection.convention(scmDeveloperConnectionFromProps)

// Register publication immediately so it's accessible during configuration
publishing {
    publications.register<MavenPublication>("mavenJava") {
        from(components["java"])
        versionMapping { allVariants { fromResolutionResult() } }
    }
}

// Configure POM details in afterEvaluate when extension values are available
afterEvaluate {
    publishing.publications.named<MavenPublication>("mavenJava") {
        pom {
            name = project.name
            description = project.description
            inceptionYear = publishConfig.inceptionYear.getOrElse("")
            url = publishConfig.projectUrl.getOrElse("")

            licenses {
                license {
                    name = publishConfig.licenseName
                        .getOrElse("The Apache License, Version 2.0")
                    url = publishConfig.licenseUrl
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
                    url = publishConfig.developerUrl.getOrElse("")
                    organization = publishConfig.developerOrganization.getOrElse("")
                    organizationUrl = publishConfig.developerOrganizationUrl.getOrElse("")
                }
            }
            scm {
                url = publishConfig.scmUrl.getOrElse("")
                connection = publishConfig.scmConnection.getOrElse("")
                developerConnection = publishConfig.scmDeveloperConnection.getOrElse("")
            }
        }
    }
}

signing {
    useInMemoryPgpKeys(
        providers.gradleProperty("signingKey").orNull
            ?: System.getenv("SIGNINGKEY")
            ?: "",
        providers.gradleProperty("signingPassword").orNull
            ?: System.getenv("SIGNINGPASSWORD")
            ?: "",
    )
    sign(publishing.publications)
}

nmcpAggregation {
    centralPortal {
        username =
            providers.gradleProperty("mavenCentralUsername").orNull
                ?: System.getenv("MAVENCENTRALUSERNAME")
                ?: ""
        password =
            providers.gradleProperty("mavenCentralPassword").orNull
                ?: System.getenv("MAVENCENTRALPASSWORD")
                ?: ""
        publishingType = "AUTOMATIC"
    }
}

listOf(
        PublishToMavenRepository::class,
        GenerateMavenPom::class,
        GenerateModuleMetadata::class,
    )
    .forEach { tasks.withType(it) { group = "publishing.other" } }

tasks.named("publish") { group = "publishing.other" }
