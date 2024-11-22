// SPDX-License-Identifier: Apache-2.0
import java.util.Properties

plugins {
    id("java")
    id("maven-publish")
    id("signing")
    id("io.freefair.maven-central.validate-poms")
    id("org.hiero.gradle.base.lifecycle")
}

java {
    withJavadocJar()
    withSourcesJar()
}

val publishSigningEnabled =
    providers.gradleProperty("publishSigningEnabled").getOrElse("false").toBoolean()

if (publishSigningEnabled) {
    signing {
        sign(publishing.publications)
        useGpgCmd()
    }
}

publishing.publications.withType<MavenPublication>().configureEach {
    versionMapping {
        // Everything published takes the versions from the resolution result.
        // These are the versions we define in 'hiero-dependency-versions'
        // and use consistently in all modules.
        allVariants { fromResolutionResult() }
    }

    suppressAllPomMetadataWarnings()

    pom {
        val devGroups = Properties()
        val developerProperties = layout.projectDirectory.file("../developers.properties")
        devGroups.load(
            providers
                .fileContents(developerProperties)
                .asText
                .orElse(
                    provider {
                        throw RuntimeException("${developerProperties.asFile} does not exist")
                    }
                )
                .get()
                .reader()
        )

        name.set(project.name)
        url = "https://hiero.org/"
        inceptionYear = "2024"

        description =
            providers
                .fileContents(layout.projectDirectory.file("../description.txt"))
                .asText
                .orElse(provider(project::getDescription))
                .map { it.replace("\n", " ").trim() }
                .orElse("")

        organization {
            name = "Hiero - a Linux Foundation Decentralized Trust project"
            url = "https://hiero.org/"
        }

        @Suppress("UnstableApiUsage") val repoName = isolated.rootProject.name

        issueManagement {
            system = "GitHub"
            url = "https://github.com/hiero-ledger/$repoName/issues"
        }

        licenses {
            license {
                name = "Apache License, Version 2.0"
                url = "https://raw.githubusercontent.com/hiero-ledger/$repoName/main/LICENSE"
            }
        }

        scm {
            connection = "scm:git:git://github.com/hiero-ledger/$repoName.git"
            developerConnection = "scm:git:ssh://github.com:hiero-ledger/$repoName.git"
            url = "https://github.com/hiero-ledger/$repoName"
        }

        developers {
            devGroups.forEach { mail, team ->
                developer {
                    id = team as String
                    name = team as String
                    email = mail as String
                    organization = "Hiero - a Linux Foundation Decentralized Trust project"
                    organizationUrl = "https://hiero.org/"
                }
            }
        }
    }
}

tasks.named("qualityCheck") { dependsOn(tasks.validatePomFiles) }

tasks.named("qualityGate") { dependsOn(tasks.validatePomFiles) }
