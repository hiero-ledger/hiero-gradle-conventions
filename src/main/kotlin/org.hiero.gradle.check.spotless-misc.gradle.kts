// SPDX-License-Identifier: Apache-2.0
import org.hiero.gradle.spotless.LicenseHeader

plugins { id("com.diffplug.spotless") }

val subDirectories =
    layout.settingsDirectory.asFile.listFiles()!!.filter {
        it.isDirectory && !it.name.startsWith(".") && !it.name.startsWith("build")
    }

spotless {
    format("misc") {
        // do not use "**/" pattern as it is not compatible with project isolation
        target(
            *listOf("*.properties")
                .plus(subDirectories.map { dir -> "${dir.name}/**/*.properties" })
                .toTypedArray()
        )
        targetExclude(
            "**/.*/**",
            "**/build/**",
            "**/node_modules/**",
            "gradle/wrapper/gradle-wrapper.properties",
        )

        trimTrailingWhitespace()
        leadingTabsToSpaces()
        endWithNewline()

        licenseHeader(LicenseHeader.HEADER_STYLE_SHELL, LicenseHeader.FIRST_LINE_REGEX_STYLE_SHELL)
    }
}
