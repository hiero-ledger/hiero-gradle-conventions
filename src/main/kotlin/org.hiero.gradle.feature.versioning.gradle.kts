// SPDX-License-Identifier: Apache-2.0
import net.swiftzer.semver.SemVer

val versionTxt = layout.projectDirectory.file("version.txt")

tasks.register("githubVersionSummary") {
    group = "versioning"

    inputs.property("productName", project.name)
    inputs.property("version", providers.fileContents(versionTxt).asText.map { it.trim() })

    if (!providers.environmentVariable("GITHUB_STEP_SUMMARY").isPresent) {
        // Do not throw an exception if running the `gradlew tasks` task
        if (project.gradle.startParameter.taskNames.contains("githubVersionSummary")) {
            throw IllegalArgumentException(
                "This task may only be run in a Github Actions CI environment! " +
                    "Unable to locate the GITHUB_STEP_SUMMARY environment variable."
            )
        }
    }
    outputs.file(providers.environmentVariable("GITHUB_STEP_SUMMARY"))

    doLast {
        val version = inputs.properties["version"] as String
        val productName = inputs.properties["productName"] as String

        outputs.files.singleFile.writeText(
            """
            ### Deployed Version Information
            
            | Artifact Name | Version Number |
            | --- | --- |
            | $productName | $version |
        """
                .trimIndent()
        )
    }
}

tasks.register("showVersion") {
    group = "versioning"

    inputs.property("version", providers.fileContents(versionTxt).asText.map { it.trim() })

    doLast { println(inputs.properties["version"]) }
}

tasks.register("versionAsPrefixedCommit") {
    group = "versioning"

    @Suppress("UnstableApiUsage")
    inputs.property(
        "commit",
        providers
            .exec { commandLine("git", "rev-parse", "HEAD") }
            .standardOutput
            .asText
            .map { it.trim().substring(0, 7) }
    )
    inputs.property("commitPrefix", providers.gradleProperty("commitPrefix").orElse("adhoc"))
    inputs.property("version", providers.fileContents(versionTxt).asText.map { it.trim() })
    outputs.file(versionTxt)

    doLast {
        val newPrerel =
            inputs.properties["commitPrefix"].toString() +
                ".x" +
                inputs.properties["commit"].toString().take(8)
        val currVer = SemVer.parse(inputs.properties["version"] as String)
        val newVer = SemVer(currVer.major, currVer.minor, currVer.patch, newPrerel)
        outputs.files.singleFile.writeText(newVer.toString())
    }
}

tasks.register("versionAsSnapshot") {
    group = "versioning"

    inputs.property("version", providers.fileContents(versionTxt).asText.map { it.trim() })
    outputs.file(versionTxt)

    doLast {
        val currVer = SemVer.parse(inputs.properties["version"] as String)
        val newVer = SemVer(currVer.major, currVer.minor, currVer.patch, "SNAPSHOT")

        outputs.files.singleFile.writeText(newVer.toString())
    }
}

tasks.register("versionAsSpecified") {
    group = "versioning"

    inputs.property("newVersion", providers.gradleProperty("newVersion")).optional(true)

    doLast {
        val newVer =
            inputs.properties["newVersion"] as String?
                ?: throw IllegalArgumentException(
                    "No newVersion property provided! " +
                        "Please add the parameter -PnewVersion=<version> when running this task."
                )
        outputs.files.singleFile.writeText(SemVer.parse(newVer).toString())
    }
}
