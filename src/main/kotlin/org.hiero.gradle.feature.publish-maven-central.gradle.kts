// SPDX-License-Identifier: Apache-2.0
plugins {
    id("org.hiero.gradle.base.lifecycle")
    id("org.hiero.gradle.base.version")
    id("java")
    id("maven-publish")
    id("com.gradleup.nmcp")
}

val newPublishing = gradle.startParameter.taskNames.contains("publishAggregationToCentralPortal")

configurations.nmcpProducer {
    extendsFrom(configurations.implementation.get())
    extendsFrom(configurations.runtimeOnly.get())
}

tasks.withType<PublishToMavenRepository>().configureEach {
    // Publishing tasks are only enabled if we publish to the matching group.
    // Otherwise, Nexus configuration and credentials do not fit.
    val publishingPackageGroup = providers.gradleProperty("publishingPackageGroup").orNull
    enabled = newPublishing || publishingPackageGroup == project.group
}

publishing.publications.create<MavenPublication>("maven") { from(components["java"]) }

tasks.named("releaseMavenCentral") { dependsOn(tasks.named("publishToSonatype")) }

// Snapshots are published directly to Sonatype and not to a local folder first.
// https://github.com/GradleUp/nmcp/issues/61
if (version.toString().endsWith("-SNAPSHOT")) {
    publishing.repositories.named<MavenArtifactRepository>("nmcp") {
        url = uri("https://central.sonatype.com/repository/maven-snapshots")
        credentials {
            username = providers.environmentVariable("NEXUS_USERNAME").orNull
            password = providers.environmentVariable("NEXUS_PASSWORD").orNull
        }
    }
}
