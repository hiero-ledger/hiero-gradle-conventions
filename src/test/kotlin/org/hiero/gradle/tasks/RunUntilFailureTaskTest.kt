// SPDX-License-Identifier: Apache-2.0
package org.hiero.gradle.tasks

import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

/**
 * Functional tests for [RunUntilFailureTask].
 *
 * Each test uses two separate Gradle projects:
 *  - **outer** (`projectDir/`): applies the `run-until-failure` plugin via TestKit's
 *    [GradleRunner.withPluginClasspath] and hosts the `runUntilFailure` task.
 *  - **target** (`projectDir/target/`): a standalone Gradle build that the Tooling
 *    API sub-build connects to.  It does NOT reference the plugin, so the sub-build
 *    can configure without needing the plugin on its classpath.
 *
 * This two-project layout is necessary because the Tooling API launches a fresh
 * Gradle process that does not inherit TestKit's injected plugin classpath.  In a
 * real project the plugin is resolved via `includeBuild` or a plugin repository,
 * which both processes can access — but that approach is too slow for unit tests
 * (each sub-build would re-resolve the included build).
 */
class RunUntilFailureTaskTest {

    @TempDir
    @JvmField
    var projectDir: File? = null

    private lateinit var buildFile: File
    private lateinit var targetDir: File

    @BeforeEach
    fun setup() {
        val dir = projectDir!!
        buildFile = dir.resolve("build.gradle.kts")
        targetDir = dir.resolve("target")
        targetDir.mkdirs()
    }

    @Test
    fun `task reaches max retries if no failure`() {
        writeTargetProject()
        writeTargetTest("PassingTest", passing = true)
        writeOuterBuildFile(maxRetries = 2)

        val result = runBuild("runUntilFailure")

        assertThat(result.output).contains("🔁  Run attempt 1 / 2")
        assertThat(result.output).contains("🔁  Run attempt 2 / 2")
        assertThat(result.output).contains("🏁  Reached max retries (2) without a failure.")
        assertThat(result.task(":runUntilFailure")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }

    @Test
    fun `task stops on failure`() {
        writeTargetProject()
        writeTargetTest("FailingTest", passing = false)
        writeOuterBuildFile(maxRetries = 5)

        val result = runBuildAndFail("runUntilFailure")

        assertThat(result.output).contains("🔁  Run attempt 1 / 5")
        assertThat(result.output).contains("❌  FAILURE detected on attempt 1 / 5 — stopping.")
        assertThat(result.task(":runUntilFailure")?.outcome).isEqualTo(TaskOutcome.FAILED)
    }

    @Test
    fun `task respects absolute subproject task path`() {
        writeTargetMultiProject()
        writeTargetTest("SubTest", passing = true, subproject = "sub")
        writeOuterBuildFile(maxRetries = 1, testTaskName = ":sub:test")

        val result = runBuild("runUntilFailure")

        assertThat(result.output).contains("Run attempt 1 / 1")
        assertThat(result.output).contains(":sub:test")
        assertThat(result.output).contains("✅  attempt 1 / 1 passed.")
    }

    @Test
    fun `subproject task resolves relative task name`() {
        writeTargetMultiProject()
        writeTargetTest("SubTest", passing = true, subproject = "sub")
        // hostProjectPath = ":sub" makes the task resolve "test" → ":sub:test"
        writeOuterBuildFile(maxRetries = 1, hostProjectPath = ":sub")

        val result = runBuild("runUntilFailure")

        assertThat(result.output).contains("Run attempt 1 / 1")
        assertThat(result.output).contains(":sub:test")
        assertThat(result.output).contains("✅  attempt 1 / 1 passed.")
    }

    @Test
    fun `task forwards test filters`() {
        writeTargetProject()
        writeTargetTest("PassingTest", passing = true)
        writeOuterBuildFile(maxRetries = 1, filters = listOf("PassingTest.test"))

        val result = runBuild("runUntilFailure")

        assertThat(result.output).contains("Test filters : PassingTest.test")
        assertThat(result.output).contains("✅  attempt 1 / 1 passed.")
        assertThat(result.task(":runUntilFailure")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }

    // -- Target project (Tooling API sub-build) ------------------------------

    /** Single-project target with java-library and JUnit 5. */
    private fun writeTargetProject() {
        targetDir.resolve("settings.gradle.kts").writeText(
            """rootProject.name = "target"""".trimIndent()
        )
        targetDir.resolve("build.gradle.kts").writeText(javaTestBuildScript())
    }

    /** Multi-project target with a `sub` subproject. */
    private fun writeTargetMultiProject() {
        targetDir.resolve("settings.gradle.kts").writeText(
            """
            rootProject.name = "target"
            include("sub")
        """.trimIndent()
        )
        targetDir.resolve("build.gradle.kts").writeText("// root project")
        val subDir = targetDir.resolve("sub")
        subDir.mkdirs()
        subDir.resolve("build.gradle.kts").writeText(javaTestBuildScript())
    }

    private fun writeTargetTest(
        className: String,
        passing: Boolean,
        subproject: String? = null
    ) {
        val base = if (subproject != null) targetDir.resolve(subproject) else targetDir
        val testDir = base.resolve("src/test/java")
        testDir.mkdirs()
        val assertion = if (passing) "assertTrue(true);" else "assertTrue(false);"
        testDir.resolve("$className.java").writeText(
            """
            import org.junit.jupiter.api.Test;
            import static org.junit.jupiter.api.Assertions.assertTrue;

            public class $className {
                @Test void test() { $assertion }
            }
        """.trimIndent()
        )
    }

    // -- Outer project (TestKit) ---------------------------------------------

    private fun writeOuterBuildFile(
        maxRetries: Int = 0,
        testTaskName: String? = null,
        hostProjectPath: String? = null,
        filters: List<String> = emptyList()
    ) {
        val taskNameLine = if (testTaskName != null)
            """testTaskName.set("$testTaskName")""" else ""
        val hostPathLine = if (hostProjectPath != null)
            """hostProjectPath.set("$hostProjectPath")""" else ""
        val filterLine = if (filters.isNotEmpty())
            """runUntilFailure { testFilters.set(listOf(${filters.joinToString { "\"$it\"" }})) }"""
        else ""

        buildFile.writeText(
            """
            plugins { id("org.hiero.gradle.feature.run-until-failure") }

            runUntilFailure { maxRetries.set($maxRetries) }
            $filterLine

            tasks.named<org.hiero.gradle.tasks.RunUntilFailureTask>("runUntilFailure") {
                projectDir.set(file("target"))
                $taskNameLine
                $hostPathLine
            }
        """.trimIndent()
        )
    }

    // -- Helpers --------------------------------------------------------------

    private fun javaTestBuildScript(): String =
        """
        plugins { `java-library` }
        repositories { mavenCentral() }
        dependencies {
            testImplementation(platform("org.junit:junit-bom:5.10.2"))
            testImplementation("org.junit.jupiter:junit-jupiter")
            testRuntimeOnly("org.junit.platform:junit-platform-launcher")
        }
        tasks.named<Test>("test") { useJUnitPlatform() }
    """.trimIndent()

    private fun runBuild(vararg args: String): BuildResult =
        GradleRunner.create()
            .withProjectDir(projectDir)
            .withPluginClasspath()
            .withArguments(*args)
            .build()

    private fun runBuildAndFail(vararg args: String): BuildResult =
        GradleRunner.create()
            .withProjectDir(projectDir)
            .withPluginClasspath()
            .withArguments(*args)
            .buildAndFail()
}
