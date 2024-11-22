// SPDX-License-Identifier: Apache-2.0
plugins { id("com.diffplug.spotless") }

spotless {
    kotlinGradle {
        ktfmt().kotlinlangStyle()

        licenseHeader(
            "// SPDX-License-Identifier: Apache-2.0\n",
            "(import|plugins|pluginManagement|dependencyResolutionManagement|repositories|tasks|allprojects|subprojects|buildCache|version)"
        )
    }
}
