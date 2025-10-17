// SPDX-License-Identifier: Apache-2.0
import org.hiero.gradle.spotless.LicenseHeader

plugins { id("com.diffplug.spotless") }

spotless {
    format("rust") {
        target("src/**/*.rs")

        trimTrailingWhitespace()
        leadingTabsToSpaces()
        endWithNewline()

        // additional newline after header in 'rs' files
        licenseHeader(LicenseHeader.HEADER_STYLE_C + "\n", LicenseHeader.FIRST_LINE_REGEX_STYLE_C)
    }
}
