// SPDX-License-Identifier: Apache-2.0
plugins {
    id("org.hiero.gradle.base.lifecycle")
    id("org.hiero.gradle.base.jpms-modules")
    id("org.hiero.gradle.check.spotless")
    id("org.hiero.gradle.check.spotless-kotlin")
    id("org.hiero.gradle.check.versions")
}

dependencies.constraints {
    api("com.fasterxml.jackson.core:jackson-databind:2.16.0") {
        because("com.fasterxml.jackson.databind")
    }

    api("org.junit.jupiter:junit-jupiter-api:5.10.2") { because("org.junit.jupiter.api") }
}
