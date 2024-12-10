// SPDX-License-Identifier: Apache-2.0
plugins { id("com.diffplug.spotless") }

spotless {
    format("actionYaml") {
        target(".github/workflows/*.yaml", ".github/workflows/*.yml")
        /*
         * Prettier requires NodeJS and NPM installed; however, the NodeJS Gradle plugin and Spotless do not yet
         * integrate with each other. Currently there is an open issue report against spotless.
         *
         *   *** Please see for more information: https://github.com/diffplug/spotless/issues/728 ***
         *
         * The workaround provided in the above issue does not work in Gradle 7.5+ and therefore is not a viable solution.
         */
        // prettier()

        trimTrailingWhitespace()
        indentWithSpaces()
        endWithNewline()

        licenseHeader("# SPDX-License-Identifier: Apache-2.0\n", "(name)")
    }
}
