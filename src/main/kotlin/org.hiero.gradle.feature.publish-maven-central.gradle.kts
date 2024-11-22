// SPDX-License-Identifier: Apache-2.0
plugins {
    id("java")
    id("maven-publish")
    id("org.hiero.gradle.base.lifecycle")
}

tasks.withType<PublishToMavenRepository>().configureEach {
    // Publishing tasks are only enabled if we publish to the matching group.
    // Otherwise, Nexus configuration and credentials do not fit.
    val publishingPackageGroup = providers.gradleProperty("publishingPackageGroup").orNull
    enabled = publishingPackageGroup == project.group
}

publishing.publications.create<MavenPublication>("maven") { from(components["java"]) }

tasks.named("releaseMavenCentral") { dependsOn(tasks.named("publishToSonatype")) }
