// SPDX-License-Identifier: Apache-2.0
import org.hiero.gradle.spotless.LicenseHeader

plugins { id("com.diffplug.spotless") }

spotless {
    kotlinGradle {
        ktfmt().kotlinlangStyle()

        licenseHeader(
                LicenseHeader.javaFormat(project),
                "(import|plugins|pluginManagement|dependencyResolutionManagement|repositories|tasks|allprojects|subprojects|buildCache|version)"
            )
            .updateYearWithLatest(true)
    }
}
