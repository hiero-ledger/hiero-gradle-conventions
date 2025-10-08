// SPDX-License-Identifier: Apache-2.0
package org.hiero.gradle.test

import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.TaskOutcome
import org.hiero.gradle.test.fixtures.GradleProject
import org.junit.jupiter.api.Test

class AggregationTest {

    @Test
    fun `works without explicit aggregation definition`() {
        val p = GradleProject().withMinimalStructure()
        p.dependencyVersionsFile(
            """
            dependencies.constraints {
                api("com.github.spotbugs:spotbugs-annotations:4.9.3")
            }"""
                .trimIndent()
        )
        p.moduleBuildFile("""plugins { id("org.hiero.gradle.module.library") }""")
        p.moduleInfoFile(
            """
            module org.hiero.product.module.a {
                requires com.github.spotbugs.annotations;
            }"""
                .trimIndent()
        )

        val result = p.run("assemble")

        assertThat(result.task(":module-a:compileJava")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }
}
