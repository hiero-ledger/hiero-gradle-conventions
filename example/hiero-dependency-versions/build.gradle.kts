// SPDX-License-Identifier: Apache-2.0
plugins {
    id("org.hiero.gradle.base.lifecycle")
    id("org.hiero.gradle.base.jpms-modules")
}

dependencies.constraints {
    api("com.fasterxml.jackson.core:jackson-databind:2.16.0") {
        because("com.fasterxml.jackson.databind")
    }
}
