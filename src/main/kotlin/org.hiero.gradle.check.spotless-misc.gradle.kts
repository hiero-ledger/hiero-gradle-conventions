// SPDX-License-Identifier: Apache-2.0
import org.hiero.gradle.spotless.LicenseHeader

plugins { id("com.diffplug.spotless") }

spotless {
    format("misc") {
        // do not use "**/" pattern as it is not compatible with project isolation
        target(
            layout.projectDirectory.asFileTree.matching {
                include("**/*.properties")
                exclude("**/.*/**")
                exclude("**/build/**")
                exclude("**/node_modules/**")
                exclude("gradle/wrapper/**")
            }
        )

        trimTrailingWhitespace()
        leadingTabsToSpaces()
        endWithNewline()

        licenseHeader(LicenseHeader.HEADER_STYLE_SHELL, LicenseHeader.FIRST_LINE_REGEX_STYLE_SHELL)
    }
}
