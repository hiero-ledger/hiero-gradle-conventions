// SPDX-License-Identifier: Apache-2.0
package org.hiero.gradle.environment

import java.util.Properties
import org.gradle.api.file.Directory
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.ProviderFactory

object EnvAccess {

    fun toolchainVersions(
        rootDir: Directory,
        providers: ProviderFactory,
        objects: ObjectFactory,
    ): MapProperty<String, String> {
        val versionsFile = rootDir.file("gradle/toolchain-versions.properties")
        val properties =
            Properties().also {
                it.load(providers.fileContents(versionsFile).asText.getOrElse("").reader())
            }

        val versions = objects.mapProperty(String::class.java, String::class.java)
        properties.forEach { (key, value) -> versions.put(key as String, value as String) }
        return versions
    }

    fun isCiServer(providers: ProviderFactory) =
        providers.environmentVariable("CI").getOrElse("false").let {
            if (it.isBlank()) true else it.toBoolean()
        }

    fun isGitRepositoryWithMainBranch(projectDirectory: Directory, providers: ProviderFactory) =
        providers
            .exec {
                isIgnoreExitValue = true
                commandLine("git", "rev-parse", "origin/main")
                workingDir = projectDirectory.asFile
            }
            .result
            .get()
            .exitValue == 0
}
