// SPDX-License-Identifier: Apache-2.0
package org.hiero.gradle.test

import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.TaskOutcome
import org.hiero.gradle.test.fixtures.GradleProject
import org.junit.jupiter.api.Test

class ExampleTest {

    @Test
    fun `example project is working`() {
        val p = GradleProject().copyFromFolder("example")

        val result = p.qualityCheck()

        assertThat(result.task(":module-app:qualityCheck")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
        assertThat(result.task(":module-lib:qualityCheck")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }
}
