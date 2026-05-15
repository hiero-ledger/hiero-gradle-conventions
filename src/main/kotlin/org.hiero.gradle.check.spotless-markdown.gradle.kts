// SPDX-License-Identifier: Apache-2.0
plugins { id("com.diffplug.spotless") }

val subDirectories =
    layout.settingsDirectory.asFile.listFiles()!!.filter {
        it.isDirectory && !it.name.startsWith(".") && !it.name.startsWith("build")
    }

spotless {
    flexmark {
        // do not use "**/" pattern as it is not compatible with project isolation
        target(
            *listOf("*.md").plus(subDirectories.map { dir -> "${dir.name}/**/*.md" }).toTypedArray()
        )
        targetExclude("**/.*/**", "**/build/**", "**/node_modules/**", "platform-sdk/sdk/**")
        flexmark().extensions("YamlFrontMatter")
        trimTrailingWhitespace()
        leadingTabsToSpaces()
        endWithNewline()
    }
}
