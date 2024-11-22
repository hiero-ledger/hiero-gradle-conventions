// SPDX-License-Identifier: Apache-2.0
plugins { id("com.gradle.develocity") version "3.18.2" }

rootProject.name = "hiero-gradle-conventions"

develocity {
    buildScan {
        termsOfUseUrl = "https://gradle.com/help/legal-terms-of-use"
        termsOfUseAgree = "yes"
        // Enable Gradle Build Scan only with explicit '--scan'
        publishing.onlyIf { false }
    }
}

dependencyResolutionManagement { @Suppress("UnstableApiUsage") repositories.gradlePluginPortal() }
