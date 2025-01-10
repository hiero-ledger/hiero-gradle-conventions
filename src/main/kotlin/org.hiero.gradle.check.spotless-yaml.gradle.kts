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
        indentWithSpaces()
        endWithNewline()

        licenseHeader(LicenseHeader.yamlFormat(project), "\\w+\\:").updateYearWithLatest(true)
    }
}

// 'dependsOn' is required because prettier().npmExecutable() does not preserve task dependency
tasks.named("spotlessYaml") { dependsOn(tasks.npmSetup) }
