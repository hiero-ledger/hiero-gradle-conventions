// SPDX-License-Identifier: Apache-2.0
import org.hiero.gradle.spotless.LicenseHeader

plugins { id("com.diffplug.spotless") }

spotless {
    format("misc") {
        target("**/*.properties")
        targetExclude("gradle/wrapper/gradle-wrapper.properties")

        trimTrailingWhitespace()
        leadingTabsToSpaces()
        endWithNewline()

        licenseHeader(LicenseHeader.HEADER_STYLE_SHELL, LicenseHeader.FIRST_LINE_REGEX_STYLE_SHELL)
    }
}
