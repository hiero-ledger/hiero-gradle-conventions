// SPDX-License-Identifier: Apache-2.0
import org.hiero.gradle.tasks.JavaVersionConsistencyCheck

plugins {
    id("org.hiero.gradle.base.lifecycle")
    id("org.hiero.gradle.base.jpms-modules")
}

tasks.register<JavaVersionConsistencyCheck>("checkVersionConsistency") {
    group = "verification"

    definedVersions = provider {
        configurations["api"].dependencyConstraints.associate {
            "${it.group}:${it.name}" to it.version!!
        }
    }
    aggregatedClasspath = provider {
        configurations["mainRuntimeClasspath"].incoming.resolutionResult.allComponents
    }
    reportFile = layout.buildDirectory.file("reports/version-consistency.txt")
}

tasks.named("qualityCheck") { dependsOn(tasks.named("checkVersionConsistency")) }

tasks.named("qualityGate") { dependsOn(tasks.named("checkVersionConsistency")) }
