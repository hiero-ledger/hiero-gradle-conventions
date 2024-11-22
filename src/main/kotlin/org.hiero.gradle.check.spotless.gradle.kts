// SPDX-License-Identifier: Apache-2.0
plugins {
    id("org.hiero.gradle.base.lifecycle")
    id("com.diffplug.spotless")
}

spotless {
    // Disable the automatic application of Spotless to all source sets when the check task is run.
    isEnforceCheck = false

    // limit format enforcement to just the files changed by this feature branch
    @Suppress("UnstableApiUsage")
    ratchetFrom(
        "origin/" +
            providers
                .fileContents(
                    isolated.rootProject.projectDirectory.file("gradle/development-branch.txt")
                )
                .asText
                .getOrElse("main")
    )
}

tasks.withType<JavaCompile>().configureEach {
    // When doing a 'qualityGate' run, make sure spotlessApply is done before doing compilation and
    // other checks based on compiled code
    mustRunAfter(tasks.spotlessApply)
}

tasks.named("qualityCheck") { dependsOn(tasks.spotlessCheck) }

tasks.named("qualityGate") { dependsOn(tasks.spotlessApply) }
