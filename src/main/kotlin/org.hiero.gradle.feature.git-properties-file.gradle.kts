// SPDX-License-Identifier: Apache-2.0
import org.hiero.gradle.environment.EnvAccess

plugins { id("java") }

tasks.register<WriteProperties>("writeGitProperties") {
    property("git.build.version", project.version)
    property(
        "git.commit.id",
        providers
            .exec {
                commandLine("git", "rev-parse", "HEAD")
                workingDir = layout.projectDirectory.asFile
            }
            .standardOutput
            .asText
            .map { it.trim() },
    )
    property(
        "git.commit.id.abbrev",
        providers
            .exec {
                commandLine("git", "rev-parse", "HEAD")
                workingDir = layout.projectDirectory.asFile
            }
            .standardOutput
            .asText
            .map { it.trim().substring(0, 7) },
    )

    destinationFile = layout.buildDirectory.file("generated/git/git.properties")
}

if (EnvAccess.isGitRepositoryWithMainBranch(layout.projectDirectory, providers)) {
    tasks.processResources { from(tasks.named("writeGitProperties")) }
}

// ignore the content of 'git.properties' when using a classpath as task input
normalization.runtimeClasspath { ignore("git.properties") }
