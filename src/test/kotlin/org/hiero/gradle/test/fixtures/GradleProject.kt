/*
 * Copyright (C) 2024 Hiero a Series of LF Projects, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.hiero.gradle.test.fixtures

import java.io.File
import java.lang.management.ManagementFactory
import java.nio.file.Files
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner

/**
 * Access to a minimal project inside a temporary folder. The project contain files that are
 * expected to exist in our setup.
 */
class GradleProject {

    private val projectDir = Files.createTempDirectory("gradle-build").toFile()
    val problemsReport = file("build/reports/problems/problems-report.html")
    private val gradlePropertiesFile = file("gradle.properties")
    private val settingsFile = file("settings.gradle.kts")
    private val dependencyVersions = file("hedera-dependency-versions/build.gradle.kts")
    private val aggregation = file("gradle/aggregation/build.gradle.kts")
    private val versionFile = file("version.txt")
    private val jdkVersionFile = file("gradle/jdk-version.txt")

    private val developersProperties = file("product/developers.properties")
    val descriptionTxt = file("product/description.txt")
    private val moduleBuildFile = file("product/module-a/build.gradle.kts")
    private val moduleInfo = file("product/module-a/src/main/java/module-info.java")
    private val javaSourceFile =
        file("product/module-a/src/main/java/org/hiero/product/module/a/ModuleA.java")

    private val expectedHeader =
        """
           /*
            * Copyright (C) 2024 Hedera Hashgraph, LLC
            *
            * Licensed under the Apache License, Version 2.0 (the "License");
            * you may not use this file except in compliance with the License.
            * You may obtain a copy of the License at
            *
            *      http://www.apache.org/licenses/LICENSE-2.0
            *
            * Unless required by applicable law or agreed to in writing, software
            * distributed under the License is distributed on an "AS IS" BASIS,
            * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
            * See the License for the specific language governing permissions and
            * limitations under the License.
            */
    """
            .trimIndent()

    fun withMinimalStructure(): GradleProject {
        git("init")
        git("checkout", "-b", "origin/main")

        gradlePropertiesFile.writeText(
            """
            org.gradle.configuration-cache=true
            # org.gradle.unsafe.isolated-projects=true
            # org.gradle.caching=true
        """
                .trimIndent()
        )
        settingsFile(
            """
            plugins { id("org.hiero.gradle.build") }
            rootProject.name = "test-project"
            javaModules { directory("product") }
        """
                .trimIndent()
        )
        dependencyVersionsFile(
            """plugins {
            id("org.hiero.gradle.base.lifecycle")
            id("org.hiero.gradle.base.jpms-modules")
        }"""
                .trimIndent()
        )
        aggregation.writeText("")
        versionFile.writeText("1.0")
        jdkVersionFile.writeText("17.0.12")
        developersProperties.writeText("test=test@hiero.org")
        descriptionTxt.writeText("A module to test hiero-gradle-conventions")
        moduleInfo.writeText("module org.hiero.product.module.a {}")
        javaSourceFile(
            """
            package org.hiero.product.module.a;
            
            class ModuleA {
            }
        """
                .trimIndent()
        )

        git("add", "--all")
        git("commit", "-m", "initial")
        return this
    }

    private fun git(vararg args: String) {
        Runtime.getRuntime().exec(arrayOf("git", *args), emptyArray(), projectDir).waitFor()
    }

    fun settingsFile(content: String) = settingsFile.also { it.writeFormatted(content) }

    fun moduleBuildFile(content: String) = moduleBuildFile.also { it.writeFormatted(content) }

    fun moduleInfoFile(content: String) = moduleInfo.also { it.writeFormatted(content) }

    fun javaSourceFile(content: String) = javaSourceFile.also { it.writeFormatted(content) }

    fun dependencyVersionsFile(content: String) =
        dependencyVersions.also { it.writeFormatted(content) }

    fun file(path: String, content: String? = null) =
        File(projectDir, path).also {
            it.parentFile.mkdirs()
            if (content != null) {
                it.writeText(content)
            }
        }

    fun help(): BuildResult = runner(listOf("help")).build()

    fun build(): BuildResult = runner(listOf("build")).build()

    fun qualityCheck(): BuildResult = runner(listOf("qualityCheck")).build()

    fun failQualityCheck(): BuildResult = runner(listOf("qualityCheck")).buildAndFail()

    private fun File.writeFormatted(content: String) {
        writeText("$expectedHeader\n\n$content\n")
    }

    private fun runner(args: List<String>) =
        GradleRunner.create()
            .forwardOutput()
            .withPluginClasspath()
            .withProjectDir(projectDir)
            .withArguments(args + listOf("-s", "--warning-mode=all"))
            .withDebug(
                ManagementFactory.getRuntimeMXBean()
                    .inputArguments
                    .toString()
                    .contains("-agentlib:jdwp")
            )
}
