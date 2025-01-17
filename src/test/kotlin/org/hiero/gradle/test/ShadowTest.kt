// SPDX-License-Identifier: Apache-2.0
package org.hiero.gradle.test

import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.TaskOutcome
import org.hiero.gradle.test.fixtures.GradleProject
import org.junit.jupiter.api.Test

class ShadowTest {

    @Test
    fun `can build a fatjar for an application`() {
        val p = GradleProject().withMinimalStructure()
        p.dependencyVersionsFile(
            """
            plugins {
                id("org.hiero.gradle.base.lifecycle")
                id("org.hiero.gradle.base.jpms-modules")
            }
            dependencies.constraints {
                api("org.apache.commons:commons-lang3:3.14.0") { because("org.apache.commons.lang3") }
            }
            """
                .trimIndent()
        )
        p.moduleInfoFile(
            """
            module org.hiero.product.module.a {
                requires org.apache.commons.lang3;
            }"""
                .trimIndent()
        )
        p.moduleBuildFile(
            """
            plugins {
                id("org.hiero.gradle.module.application")
                id("org.hiero.gradle.feature.shadow")
            }
            application {
                mainClass = "org.hiero.product.module.a.ModuleA"
            }
        """
                .trimIndent()
        )

        val result = p.run("assemble")

        assertThat(result.task(":module-a:shadowJar")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }
}
