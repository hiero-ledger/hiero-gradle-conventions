// SPDX-License-Identifier: Apache-2.0
package org.hiero.gradle.test

import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.TaskOutcome
import org.hiero.gradle.test.fixtures.GradleProject
import org.junit.jupiter.api.Test

class ExecuteTest {

    @Test
    fun `applications run on the module path by default`() {
        val p = GradleProject().withMinimalStructure()
        p.moduleBuildFile(
            """
            plugins { id("org.hiero.gradle.module.application") }
            
            application { mainClass = "org.hiero.product.module.a.ModuleA" }
            """
                .trimIndent()
        )
        p.javaSourceFile(
            """
            package org.hiero.product.module.a;
            
            public class ModuleA {
                public static void main(String[] args) {
                    System.out.println("Module Name: " + ModuleA.class.getModule().getName());
                }
            }
        """
                .trimIndent()
        )

        val result = p.run("run")

        assertThat(result.task(":module-a:run")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
        assertThat(result.output).contains("Module Name: org.hiero.product.module.a")
    }
}
