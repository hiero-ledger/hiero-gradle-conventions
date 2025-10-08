// SPDX-License-Identifier: Apache-2.0
package org.hiero.gradle.test

import java.nio.file.Files
import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.TaskOutcome
import org.hiero.gradle.test.fixtures.GradleProject
import org.junit.jupiter.api.Test

class MinimalProjectTest {

    @Test
    fun `setup works outside of git repository`() {
        // run test in a temporary directory to run outside the git repository containing this test
        val p = GradleProject(Files.createTempDirectory("gt").toFile()).withMinimalStructure()
        p.moduleBuildFile("""plugins { id("org.hiero.gradle.module.library") }""")

        val result = p.qualityCheck()

        assertThat(result.task(":module-a:compileJava")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }

    @Test
    fun `uses defaults in a minimal project`() {
        val p = GradleProject()
        p.settingsFile("plugins { id(\"org.hiero.gradle.build\") }")

        val result = p.noTask()

        assertThat(result.output)
            .contains(
                """
            
            Build tasks
            -----------
            assemble - Assembles the outputs of this project.
            build - Assembles and tests this project.
            clean - Deletes the build directory.
            qualityGate - Apply spotless rules and run all quality checks.
            test - Runs the test suite.
            
        """
                    .trimIndent()
            )
    }

    @Test
    fun `uses defaults in a minimal project with a module`() {
        val p = GradleProject()
        p.settingsFile(
            """
            plugins { id("org.hiero.gradle.build") }
            
            javaModules { directory("product") }"""
                .trimIndent()
        )

        p.moduleInfoFile("module org.hiero.product.module.a {}")
        p.moduleBuildFile(
            """
            plugins { id("org.hiero.gradle.module.library") }
            
            description = "A module that is now described"
            """
                .trimIndent()
        )
        p.javaSourceFile(
            """
            package org.hiero.product.module.a;
            
            public class ModuleA {}
        """
                .trimIndent()
        )

        val result = p.qualityCheck()

        assertThat(result.output)
            .contains(
                """
                WARN: No version pinned in version.txt file (using: 0.1.0-SNAPSHOT)
                 - Run: ./gradlew versionAsSpecified -PnewVersion=0.1.0
                WARN: No 'jdk' version pinned (using: 
        """
                    .trimIndent()
            )
    }
}
