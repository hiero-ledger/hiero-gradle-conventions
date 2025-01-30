// SPDX-License-Identifier: Apache-2.0
package org.hiero.gradle.versions

import java.util.Properties
import org.gradle.api.file.Directory
import org.gradle.api.provider.ProviderFactory

object Versions {

    fun toolchainVersions(rootDir: Directory, providers: ProviderFactory): Map<Any, Any> {
        val versionsFile = rootDir.file("gradle/toolchain-versions.properties")
        return Properties().also {
            it.load(
                providers
                    .fileContents(versionsFile)
                    .asText
                    .orElse(
                        providers.provider {
                            throw RuntimeException("${versionsFile.asFile} does not exist")
                        }
                    )
                    .get()
                    .reader()
            )
        }
    }
}
