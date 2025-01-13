// SPDX-License-Identifier: Apache-2.0
package org.hiero.gradle.test.fixtures

import java.io.File
import java.lang.management.ManagementFactory
import java.util.UUID
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner

/**
 * Access to a minimal project inside a temporary folder. The project contain files that are
 * expected to exist in our setup.
 */
class GradleProject {

    private val projectDir = File("build/test-projects/${UUID.randomUUID()}")

    val problemsReport = file("build/reports/problems/problems-report.html")
    private val gradlePropertiesFile = file("gradle.properties")
    val settingsFile = file("settings.gradle.kts")
    private val dependencyVersions = file("hiero-dependency-versions/build.gradle.kts")
    private val aggregation = file("gradle/aggregation/build.gradle.kts")
    val versionFile = file("version.txt")
    private val toolchainVersionsFile = file("gradle/toolchain-versions.properties")

    val descriptionTxt = file("product/description.txt")
    val moduleBuildFile = file("product/module-a/build.gradle.kts")
    private val moduleInfo = file("product/module-a/src/main/java/module-info.java")
    private val javaSourceFile =
        file("product/module-a/src/main/java/org/hiero/product/module/a/ModuleA.java")

    private val expectedHeader = "// SPDX-License-Identifier: Apache-2.0\n"

    private val env = mutableMapOf<String, String>()

    fun withMinimalStructure(): GradleProject {
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
            
            javaModules { directory("product") { group = "org.example" } }
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
        aggregation.writeFormatted("""plugins { id("org.hiero.gradle.base.lifecycle") }""")
        versionFile.writeText("1.0")
        toolchainVersionsFile.writeText("jdk=17.0.12")
        descriptionTxt.writeText("A module to test hiero-gradle-conventions")
        moduleInfoFile("module org.hiero.product.module.a {}")
        javaSourceFile(
            """
            package org.hiero.product.module.a;
            
            class ModuleA {}
        """
                .trimIndent()
        )

        return this
    }

    fun withEnv(env: Map<String, String>): GradleProject {
        this.env.putAll(env)
        return this
    }

    fun copyFromFolder(folderName: String): GradleProject {
        projectDir.deleteRecursively()
        File(folderName).copyRecursively(projectDir)
        return this
    }

    fun settingsFile(content: String) = settingsFile.also { it.writeFormatted(content) }

    fun moduleBuildFile(content: String) = moduleBuildFile.also { it.writeFormatted(content) }

    fun moduleInfoFile(content: String) = moduleInfo.also { it.writeFormatted(content) }

    fun javaSourceFile(content: String) = javaSourceFile.also { it.writeFormatted(content) }

    fun dependencyVersionsFile(content: String) =
        dependencyVersions.also { it.writeFormatted(content) }

    fun aggregationBuildFile(content: String) = aggregation.also { it.writeFormatted(content) }

    fun toolchainVersionsFile(content: String) =
        toolchainVersionsFile.also { it.writeText(content) }

    fun file(path: String, content: String? = null) =
        File(projectDir, path).also {
            it.parentFile.mkdirs()
            if (content != null) {
                it.writeText(content)
            }
        }

    fun help(): BuildResult = runner(listOf("help")).build()

    fun check(): BuildResult = runner(listOf("check")).build()

    fun qualityCheck(): BuildResult = runner(listOf("qualityCheck")).build()

    fun failQualityCheck(): BuildResult = runner(listOf("qualityCheck")).buildAndFail()

    fun qualityGate(): BuildResult = runner(listOf("qualityGate")).build()

    fun run(params: String): BuildResult = runner(params.split(" ")).build()

    fun runAndFail(params: String): BuildResult = runner(params.split(" ")).buildAndFail()

    private fun File.writeFormatted(content: String) {
        writeText("$expectedHeader$content\n")
    }

    private fun runner(args: List<String>): GradleRunner {
        val debugMode =
            ManagementFactory.getRuntimeMXBean()
                .inputArguments
                .toString()
                .contains("-agentlib:jdwp")
        return GradleRunner.create()
            .forwardOutput()
            .withPluginClasspath()
            .withProjectDir(projectDir)
            .withArguments(args + listOf("-s", "--warning-mode=all"))
            .withDebug(debugMode)
            .let { if (env.isEmpty() || debugMode) it else it.withEnvironment(env) }
    }
}
