// SPDX-License-Identifier: Apache-2.0
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

tasks.withType<Sign>().configureEach { enabled = publishSigningEnabled }

signing {
    sign(publishing.publications)
    useGpgCmd()
}

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
            providers
                .environmentVariable("GITHUB_REPOSITORY")
                .map { it.substringBefore("/") }
                .orElse(providers.gradleProperty("publishGitHubOrg"))
                .orElse("hiero-ledger")

        issueManagement {
            system = "GitHub"
            url = "https://github.com/${gitHubOrg.get()}/$repoName/issues"
        }

        licenses {
            license {
                name = "Apache License, Version 2.0"
                url = "https://raw.githubusercontent.com/${gitHubOrg.get()}/$repoName/main/LICENSE"
            }
        }

        scm {
            connection = "scm:git:git://github.com/${gitHubOrg.get()}/$repoName.git"
            developerConnection = "scm:git:ssh://github.com:${gitHubOrg.get()}/$repoName.git"
            url = "https://github.com/${gitHubOrg.get()}/$repoName"
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
