// SPDX-License-Identifier: Apache-2.0
plugins { id("com.diffplug.spotless") }

spotless {
    format("misc") {
        // define the files to apply `misc` to
        target(".gitignore")

        // define the steps to apply to those files
        trimTrailingWhitespace()
        indentWithSpaces()
        endWithNewline()
    }
}
