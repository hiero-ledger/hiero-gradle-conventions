// SPDX-License-Identifier: Apache-2.0
plugins {
    id("org.hiero.gradle.base.lifecycle")
    id("org.hiero.gradle.base.version")
    id("java")
    id("maven-publish")
    id("com.gradleup.nmcp")
}

configurations.nmcpProducer {
    extendsFrom(configurations.implementation.get())
    extendsFrom(configurations.runtimeOnly.get())
}

publishing.publications.create<MavenPublication>("maven") { from(components["java"]) }

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
