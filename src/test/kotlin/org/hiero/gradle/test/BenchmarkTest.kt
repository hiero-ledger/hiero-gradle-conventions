// SPDX-License-Identifier: Apache-2.0
package org.hiero.gradle.test

import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.TaskOutcome
import org.hiero.gradle.test.fixtures.GradleProject
import org.junit.jupiter.api.Test

class BenchmarkTest {

    @Test
    fun `can use jmhJar task`() {
        val p = GradleProject().withMinimalStructure()
        p.moduleBuildFile(
            """
            plugins { id("org.hiero.gradle.feature.benchmark") }
            """
                .trimIndent()
        )

        val result = p.run("jmhJar")

        assertThat(result.task(":module-a:jmhJar")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }
}
