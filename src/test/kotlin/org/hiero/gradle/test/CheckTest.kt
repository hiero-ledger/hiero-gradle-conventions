// SPDX-License-Identifier: Apache-2.0
package org.hiero.gradle.test

import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.TaskOutcome
import org.hiero.gradle.test.fixtures.GradleProject
import org.junit.jupiter.api.Test

class CheckTest {

    @Test
    fun `check produces code coverage reports`() {
        val p = GradleProject().withMinimalStructure()
        p.aggregationBuildFile(
            """
            plugins { id("org.hiero.gradle.report.code-coverage") }

            dependencies { implementation(project(":module-a")) }
        """
                .trimIndent()
        )
        p.moduleBuildFile(
            """
            plugins { id("org.hiero.gradle.module.library") }
            
            testModuleInfo { requiresStatic("org.junit.jupiter.api") }
            """
                .trimIndent()
        )
        p.dependencyVersionsFile(
            """
            plugins {
                id("org.hiero.gradle.base.lifecycle")
                id("org.hiero.gradle.base.jpms-modules")
            }
            dependencies.constraints {
                api("org.junit.jupiter:junit-jupiter-api:5.10.2") { because("org.junit.jupiter.api") }
                api("org.junit.jupiter:junit-jupiter-engine:5.10.2") { because("org.junit.jupiter.engine") }
            }"""
                .trimIndent()
        )

        p.file(
            "product/module-a/src/test/java/org/hiero/product/module/a/test/ModuleATest.java",
            """
            // SPDX-License-Identifier: Apache-2.0
            package org.hiero.product.module.a.test;
            
            class ModuleATest {
            
                @org.junit.jupiter.api.Test
                void testA() {}
            }
            
            """
                .trimIndent(),
        )

        val result = p.check()

        assertThat(result.task(":module-a:jacocoTestReport")?.outcome)
            .isEqualTo(TaskOutcome.SUCCESS)
        assertThat(p.file("product/module-a/build/reports/jacoco/test/html/index.html")).exists()
        assertThat(result.task(":aggregation:testCodeCoverageReport")?.outcome)
            .isEqualTo(TaskOutcome.SUCCESS)
        assertThat(
                p.file(
                    "gradle/aggregation/build/reports/jacoco/testCodeCoverageReport/html/index.html"
                )
            )
            .exists()
        assertThat(
                p.file(
                    "gradle/aggregation/build/reports/jacoco/testCodeCoverageReport/testCodeCoverageReport.xml"
                )
            )
            .exists()
    }
}
