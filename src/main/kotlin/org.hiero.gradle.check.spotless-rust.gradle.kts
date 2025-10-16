// SPDX-License-Identifier: Apache-2.0
import org.hiero.gradle.spotless.LicenseHeader

plugins { id("com.diffplug.spotless") }

spotless {
    format("rust") {
        target("src/**/*.rs")

        trimTrailingWhitespace()
        leadingTabsToSpaces()
        endWithNewline()

        licenseHeader(LicenseHeader.rustFormat(project), "^(?!\\/\\/ SPDX).")
            .updateYearWithLatest(true)
    }
}
