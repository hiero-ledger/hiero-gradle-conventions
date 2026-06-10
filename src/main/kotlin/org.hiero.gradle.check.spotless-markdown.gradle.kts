// SPDX-License-Identifier: Apache-2.0
plugins { id("com.diffplug.spotless") }

spotless {
    flexmark {
        // do not use "**/" pattern as it is not compatible with project isolation
        target(
            layout.projectDirectory.asFileTree.matching {
                include("**/*.md")
                exclude("**/.*/**")
                exclude("**/build/**")
                exclude("**/node_modules/**")
                exclude("platform-sdk/sdk")
            }
        )

        flexmark().extensions("YamlFrontMatter")
        trimTrailingWhitespace()
        leadingTabsToSpaces()
        endWithNewline()
    }
}
