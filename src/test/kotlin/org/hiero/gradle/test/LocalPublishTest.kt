// SPDX-License-Identifier: Apache-2.0
package org.hiero.gradle.test

import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.TaskOutcome
import org.hiero.gradle.test.fixtures.GradleProject
import org.junit.jupiter.api.Test

class LocalPublishTest {

    @Test
    fun `can perform a local publish of library without signing keys`() {
        val p = GradleProject().withMinimalStructure()
        p.moduleBuildFile("""plugins { id("org.hiero.gradle.module.library") }""")

        val result = p.run("publishToMavenLocal -Dmaven.repo.local=${p.file(".m2").absolutePath}")

        assertThat(result.task(":module-a:publishMavenPublicationToMavenLocal")?.outcome)
            .isEqualTo(TaskOutcome.SUCCESS)
        assertThat(p.file(".m2/org/example/module-a/1.0/module-a-1.0.jar")).exists()
    }

    @Test
    fun `can perform a local publish of gradle plugin without signing keys`() {
        val p = GradleProject().withMinimalStructure()
        p.moduleBuildFile("""plugins { id("org.hiero.gradle.module.gradle-plugin") }""")

        val result = p.run("publishToMavenLocal -Dmaven.repo.local=${p.file(".m2").absolutePath}")

        assertThat(result.task(":module-a:publishPluginMavenPublicationToMavenLocal")?.outcome)
            .isEqualTo(TaskOutcome.SUCCESS)
        assertThat(p.file(".m2/org/example/module-a/1.0/module-a-1.0.jar")).exists()
    }
}
