// SPDX-License-Identifier: Apache-2.0
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
                    val message =
                        "No version pinned in version.txt file (using: 0.1.0-SNAPSHOT)" +
                            "\n - Run: ./gradlew versionAsSpecified -PnewVersion=0.1.0"
                    logger.warn("WARN: $message")
                    "0.1.0-SNAPSHOT" // fallback value
                }
            }
        )
        .get()
        .trim()
