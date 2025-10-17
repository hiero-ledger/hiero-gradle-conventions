// SPDX-License-Identifier: Apache-2.0
import org.hiero.gradle.environment.EnvAccess

plugins {
    id("org.hiero.gradle.base.lifecycle")
    id("com.diffplug.spotless")
}

spotless {
    if (EnvAccess.isGitRepositoryWithMainBranch(layout.projectDirectory, providers)) {
        // limit format enforcement to just the files changed by this feature branch
        ratchetFrom("origin/main")
    }
}

tasks.withType<JavaCompile>().configureEach {
    // When doing a 'qualityGate' run, make sure spotlessApply is done before doing compilation and
    // other checks based on compiled code
    shouldRunAfter(tasks.spotlessApply)
}

tasks.named("qualityCheck") { dependsOn(tasks.spotlessCheck) }

tasks.named("qualityGate") { dependsOn(tasks.spotlessApply) }
