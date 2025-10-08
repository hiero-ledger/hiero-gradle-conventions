// SPDX-License-Identifier: Apache-2.0
package org.hiero.gradle.test

import org.assertj.core.api.Assertions.assertThat
import org.hiero.gradle.test.fixtures.GradleProject
import org.junit.jupiter.api.Test

class MinimalProjectTest {

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
            WARN: version.txt file not found! Run: ./gradlew versionAsSpecified -PnewVersion=<version>
            WARN: No 'jdk' version defined in 'gradle/toolchain-versions.properties'. Using:
        """
                    .trimIndent()
            )
    }
}
