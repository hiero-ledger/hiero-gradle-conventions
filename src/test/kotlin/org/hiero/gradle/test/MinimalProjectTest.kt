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
}
