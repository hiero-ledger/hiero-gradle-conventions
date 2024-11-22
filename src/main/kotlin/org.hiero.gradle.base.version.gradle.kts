// SPDX-License-Identifier: Apache-2.0
version =
    providers
        .fileContents(isolated.rootProject.projectDirectory.file("version.txt"))
        .asText
        .orElse(
            provider {
                if (project.parent == null) {
                    ""
                } else {
                    throw RuntimeException("version.txt file not found")
                }
            }
        )
        .get()
        .trim()
