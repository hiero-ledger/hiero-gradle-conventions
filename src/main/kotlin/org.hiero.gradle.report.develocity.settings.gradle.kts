// SPDX-License-Identifier: Apache-2.0
plugins { id("com.gradle.develocity") }

develocity {
    buildScan {
        val publishBuildScan =
            providers.gradleProperty("scan").getOrElse("false").let {
                if (it.isBlank()) true else it.toBoolean()
            }

        termsOfUseUrl = "https://gradle.com/help/legal-terms-of-use"
        termsOfUseAgree = "yes"
        // Enable Gradle Build Scan only with explicit '-Pscan'
        publishing.onlyIf { publishBuildScan }
    }
}

if (gradle.startParameter.isBuildScan) {
    logger.lifecycle(
        "WARNING: running with '--scan' has negative effects on build caching, use '-Pscan' instead"
    )
}
