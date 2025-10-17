// SPDX-License-Identifier: Apache-2.0
package org.hiero.gradle.test.fixtures

import java.io.File
import java.lang.management.ManagementFactory
import java.util.UUID
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner

/**
 * Access to a minimal project inside a temporary folder. The project contains files that are
 * expected to exist in our setup.
 */
class GradleProject(
    private val projectDir: File = File("build/test-projects/${UUID.randomUUID()}")
) {

    val problemsReport = file("build/reports/problems/problems-report.html")
    private val gradlePropertiesFile = file("gradle.properties")
    val settingsFile = file("settings.gradle.kts")
    val dependencyVersions = file("hiero-dependency-versions/build.gradle.kts")
    val aggregation = file("gradle/aggregation/build.gradle.kts")
    val versionFile = file("version.txt")
    val toolchainVersionsFile = file("gradle/toolchain-versions.properties")

    val descriptionTxt = file("product/description.txt")
    val moduleBuildFile = file("product/module-a/build.gradle.kts")
    val moduleInfo = file("product/module-a/src/main/java/module-info.java")
    private val javaSourceFile =
        file("product/module-a/src/main/java/org/hiero/product/module/a/ModuleA.java")

    private val expectedHeader = "// SPDX-License-Identifier: Apache-2.0\n"

    private val env = mutableMapOf<String, String>()

    fun withMinimalStructure(): GradleProject {
        gradlePropertiesFile.writeText(
            """
            # SPDX-License-Identifier: Apache-2.0
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
        versionFile.writeText("1.0")
        toolchainVersionsFile.writeText(
            """
            # SPDX-License-Identifier: Apache-2.0
            jdk=17.0.16
            
        """
                .trimIndent()
        )
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
        this.env["PATH"] = System.getenv("PATH") // some plugins use low-level commands like 'uname'
        this.env.putAll(env)
        return this
    }

    fun copyFromFolder(folderName: String): GradleProject {
        projectDir.deleteRecursively()
        File(folderName).copyRecursively(projectDir)
        return this
    }

    fun gradlePropertiesFile(content: String) = gradlePropertiesFile.also { it.writeText(content) }

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

    fun dir(path: String) = File(projectDir, path)

    fun help(): BuildResult = runner(listOf("help")).build()

    fun check(): BuildResult = runner(listOf("check")).build()

    fun qualityCheck(): BuildResult = runner(listOf("qualityCheck")).build()

    fun failQualityCheck(): BuildResult = runner(listOf("qualityCheck")).buildAndFail()

    fun qualityGate(): BuildResult = runner(listOf("qualityGate")).build()

    fun noTask(): BuildResult = runner(emptyList()).build()

    fun run(params: String): BuildResult = runner(params.split(" ")).build()

    fun runAndFail(params: String): BuildResult = runner(params.split(" ")).buildAndFail()

    fun runWithOldGradle(): BuildResult = runner(emptyList()).withGradleVersion("8.14.3").build()

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
