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
                        "version.txt file not found! Run: ./gradlew versionAsSpecified -PnewVersion=<version>"
                    logger.warn("WARN: $message")
                    "0.1.0-SNAPSHOT" // fallback value
                }
            }
        )
        .get()
        .trim()
