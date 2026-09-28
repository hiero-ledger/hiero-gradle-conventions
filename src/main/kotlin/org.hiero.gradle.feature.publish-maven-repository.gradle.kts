// SPDX-License-Identifier: Apache-2.0
import org.gradle.security.internal.gnupg.GnupgSettings
import org.gradle.security.internal.gnupg.GnupgSignatory

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

tasks.withType<Sign>().configureEach {
    enabled = publishSigningEnabled
    // the following replaces 'signing.useGpgCmd()' for project isolation compatibility
    // https://github.com/gradle/gradle/issues/37871
    signatory(GnupgSignatory(project, "default", GnupgSettings()))
}

signing { sign(publishing.publications) }

publishing.publications.withType<MavenPublication>().configureEach {
    versionMapping {
        // Everything published takes the versions from the resolution result.
        // These are the versions we define in 'hiero-dependency-versions'
        // and use consistently in all modules.
        versionMapping { allVariants { fromResolutionOf("mainRuntimeClasspath") } }
    }

    suppressAllPomMetadataWarnings()

    pom {
        name = project.name
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
        val gitHubOrg =
            providers.environmentVariable("GITHUB_REPOSITORY_OWNER").getOrElse("hiero-ledger")

        issueManagement {
            system = "GitHub"
            url = "https://github.com/$gitHubOrg/$repoName/issues"
        }

        licenses {
            license {
                name = "Apache License, Version 2.0"
                url = "https://raw.githubusercontent.com/$gitHubOrg/$repoName/main/LICENSE"
            }
        }

        scm {
            connection = "scm:git:git://github.com/$gitHubOrg/$repoName.git"
            developerConnection = "scm:git:ssh://github.com:$gitHubOrg/$repoName.git"
            url = "https://github.com/$gitHubOrg/$repoName"
        }

        developers {
            developer {
                id = "hiero"
                name = "The Hiero Team"
                email = "info@lfdecentralizedtrust.org"
                organization = "Hiero - a Linux Foundation Decentralized Trust project"
                organizationUrl = "https://hiero.org/"
            }
        }
    }
}

tasks.named("qualityCheck") { dependsOn(tasks.validatePomFiles) }

tasks.named("qualityGate") { dependsOn(tasks.validatePomFiles) }
