// SPDX-License-Identifier: Apache-2.0
package org.hiero.gradle.spotless

import org.gradle.api.Project

object LicenseHeader {
    const val default = "SPDX-License-Identifier: Apache-2.0"

    fun javaFormat(project: Project): String {
        val plainHeader = plainHeader(project).lines()
        return if (plainHeader.size == 1) "// " + plainHeader.single()
        else
            (listOf("/*") +
                    plainHeader.map { line ->
                        when (line) {
                            "" -> " *"
                            else -> " * $line"
                        }
                    } +
                    listOf(" */", "", ""))
                .joinToString("\n")
    }

    fun yamlFormat(project: Project): String {
        val plainHeader = plainHeader(project).lines()
        return if (plainHeader.size == 1) "# " + plainHeader.single()
        else
            (listOf("##") +
                    plainHeader.map { line ->
                        when (line) {
                            "" -> "#"
                            else -> "# $line"
                        }
                    } +
                    listOf("##", "", ""))
                .joinToString("\n")
    }

    private fun plainHeader(project: Project): String {
        @Suppress("UnstableApiUsage") val rootDir = project.isolated.rootProject.projectDirectory
        val headerFile = rootDir.file("gradle/license-header.txt").asFile
        val header = if (headerFile.exists()) headerFile.readText() else default
        return header
    }
}
