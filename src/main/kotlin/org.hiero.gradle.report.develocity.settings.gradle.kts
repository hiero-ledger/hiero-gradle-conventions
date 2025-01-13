// SPDX-License-Identifier: Apache-2.0
plugins { id("com.gradle.develocity") }

develocity {
    buildScan {
        val publishBuildScan =
            providers.environmentVariable("CI").getOrElse("false").let {
                if (it.isBlank()) true else it.toBoolean()
            }

        termsOfUseUrl = "https://gradle.com/help/legal-terms-of-use"
        termsOfUseAgree = "yes"
        // Enable Gradle Build Scan only with explicit '-Pscan'
        publishing.onlyIf { publishBuildScan }
    }
}
