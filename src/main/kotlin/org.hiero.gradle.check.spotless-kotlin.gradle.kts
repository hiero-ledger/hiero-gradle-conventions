// SPDX-License-Identifier: Apache-2.0
import org.hiero.gradle.spotless.LicenseHeader

plugins { id("com.diffplug.spotless") }

spotless {
    kotlinGradle {
        ktfmt().kotlinlangStyle()
        licenseHeader(LicenseHeader.HEADER_STYLE_C, LicenseHeader.FIRST_LINE_REGEX_STYLE_C)
    }
}
