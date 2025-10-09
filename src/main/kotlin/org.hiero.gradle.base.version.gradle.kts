// SPDX-License-Identifier: Apache-2.0
import org.hiero.gradle.problems.ProblemReporter

version =
    @Suppress("UnstableApiUsage")
    providers
        .fileContents(isolated.rootProject.projectDirectory.file("version.txt"))
        .asText
        .orElse(
            provider {
                if (project.parent == null) {
                    ""
                } else {
                    objects
                        .newInstance<ProblemReporter>()
                        .warn(
                            "Version not Pinned",
                            "No version pinned in version.txt file (using: 0.1.0-SNAPSHOT)",
                            "version.txt",
                            "Run: ./gradlew versionAsSpecified -PnewVersion=0.1.0",
                        )
                    "0.1.0-SNAPSHOT" // fallback value
                }
            }
        )
        .get()
        .trim()
