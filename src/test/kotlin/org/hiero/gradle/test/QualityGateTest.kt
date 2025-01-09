// SPDX-License-Identifier: Apache-2.0
package org.hiero.gradle.test

import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.TaskOutcome
import org.hiero.gradle.test.fixtures.GradleProject
import org.junit.jupiter.api.Test

class QualityGateTest {

    @Test
    fun `qualityGate formats yml and yaml files`() {
        val p = GradleProject().withMinimalStructure()
        val flow1 = p.file(".github/workflows/flow1.yml", "name: Flow 1    ")
        val flow2 = p.file(".github/workflows/flow2.yaml", "name: Flow 2    ")
        val bot = p.file(".github/dependabot.yml", "updates:    ")
        val txtFile = p.file(".github/workflows/temp.txt", "name: Flow 3    ")

        val result = p.qualityGate()

        assertThat(flow1)
            .hasContent(
                """
            # SPDX-License-Identifier: Apache-2.0
            name: Flow 1
        """
                    .trimIndent()
            )
        assertThat(flow2)
            .hasContent(
                """
            # SPDX-License-Identifier: Apache-2.0
            name: Flow 2
        """
                    .trimIndent()
            )
        assertThat(bot)
            .hasContent(
                """
            # SPDX-License-Identifier: Apache-2.0
            updates:
        """
                    .trimIndent()
            )
        assertThat(txtFile).hasContent("name: Flow 3    ") // unchanged

        assertThat(result.task(":qualityGate")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }
}
