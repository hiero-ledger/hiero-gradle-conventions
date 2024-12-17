// SPDX-License-Identifier: Apache-2.0
plugins {
    id("org.hiero.gradle.build") version "0.1.1"
    `kotlin-dsl` apply false
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement { repositories { gradlePluginPortal() } }
