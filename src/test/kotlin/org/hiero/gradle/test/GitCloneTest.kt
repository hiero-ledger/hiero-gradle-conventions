// SPDX-License-Identifier: Apache-2.0
package org.hiero.gradle.test

import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.TaskOutcome
import org.hiero.gradle.test.fixtures.GradleProject
import org.junit.jupiter.api.Test

class GitCloneTest {
    @Test
    fun `can use GitClone task`() {
        val push = GradleProject().withMinimalStructure()
        push.moduleBuildFile("""plugins { id("org.hiero.gradle.feature.git-clone") }""")

        val result = push.run("cloneRemoteRepoWTag")

        assertThat(result.output).contains("Successfully cloned")
        assertThat(result.task(":cloneRemoteRepoWTag")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }
}
