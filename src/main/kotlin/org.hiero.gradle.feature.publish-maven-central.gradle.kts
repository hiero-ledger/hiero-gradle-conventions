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
