// SPDX-License-Identifier: Apache-2.0
import org.hiero.gradle.spotless.LicenseHeader

plugins {
    id("com.diffplug.spotless")
    id("com.github.node-gradle.node")
}

node {
    download = true
    distBaseUrl = null // configured in 'repositories' plugin
}

val npm =
    tasks.npmSetup.map {
        val npmExec =
            if (System.getProperty("os.name").lowercase().contains("windows")) "npm.cmd"
            else "bin/npm"
        it.npmDir.get().file(npmExec)
    }

spotless {
    format("yaml") {
        target(".github/**/*.yaml", ".github/**/*.yml")

        prettier().npmExecutable(npm)

        trimTrailingWhitespace()
        leadingTabsToSpaces()
        endWithNewline()

        licenseHeader(LicenseHeader.HEADER_STYLE_SHELL, LicenseHeader.FIRST_LINE_REGEX_STYLE_SHELL)
    }
}

// 'dependsOn' is required because prettier().npmExecutable() does not preserve task dependency
tasks.named("spotlessYaml") { dependsOn(tasks.npmSetup) }
