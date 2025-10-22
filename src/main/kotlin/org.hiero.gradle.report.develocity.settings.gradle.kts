// SPDX-License-Identifier: Apache-2.0
import org.hiero.gradle.environment.EnvAccess

plugins {
    id("com.gradle.develocity")
    id("com.gradle.common-custom-user-data-gradle-plugin")
}

develocity {
    buildScan {
        val publishBuildScan = EnvAccess.isCiServer(providers)
        termsOfUseUrl = "https://gradle.com/help/legal-terms-of-use"
        termsOfUseAgree = "yes"
        // Enable Gradle Build Scan only with explicit '-Pscan'
        publishing.onlyIf { publishBuildScan }
    }
}
