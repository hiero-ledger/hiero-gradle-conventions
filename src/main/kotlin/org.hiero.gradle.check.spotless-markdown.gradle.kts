// SPDX-License-Identifier: Apache-2.0
plugins { id("com.diffplug.spotless") }

spotless {
    flexmark {
        target("**/*.md")
        targetExclude("platform-sdk/sdk/**", "node_modules/**")
        flexmark()
        trimTrailingWhitespace()
        indentWithSpaces()
        endWithNewline()
    }
}
