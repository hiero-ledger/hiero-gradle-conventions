// SPDX-License-Identifier: Apache-2.0
plugins {
    id("org.hiero.gradle.build") version "0.7.4"
    `kotlin-dsl` apply false
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement { repositories { gradlePluginPortal() } }
