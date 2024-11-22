/*
 * Copyright (C) 2024 Hiero a Series of LF Projects, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.hiero.gradle.test

import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.TaskOutcome
import org.hiero.gradle.test.fixtures.GradleProject
import org.junit.jupiter.api.Test

class QualityCheckTest {

    @Test
    fun `qualityCheck passes for a well-defined minimal project`() {
        val p = GradleProject().withMinimalStructure()
        p.moduleBuildFile("""plugins { id("org.hiero.gradle.module.library") }""")

        val result = p.qualityCheck()

        assertThat(result.task(":module-a:qualityCheck")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }

    @Test
    fun `qualityCheck fails for invalid pom xml`() {
        val p = GradleProject().withMinimalStructure()
        p.moduleBuildFile("""plugins { id("org.hiero.gradle.module.library") }""")
        p.descriptionTxt.delete()

        val result = p.failQualityCheck()

        assertThat(result.task(":module-a:validatePomFileForMavenPublication")?.outcome)
            .isEqualTo(TaskOutcome.FAILED)
        assertThat(p.problemsReport).content().contains("Missing Element in Maven Pom")
        assertThat(p.problemsReport).content().contains("No description found")
    }

    @Test
    fun `qualityCheck fails for wrong dependency scopes`() {
        val p = GradleProject().withMinimalStructure()
        p.dependencyVersionsFile(
            """
            plugins {
                id("org.hiero.gradle.base.lifecycle")
                id("org.hiero.gradle.base.jpms-modules")
            }
            dependencies.constraints {
                api("com.fasterxml.jackson.core:jackson-databind:2.16.0") {
                    because("com.fasterxml.jackson.databind")
                }
                api("org.apache.commons:commons-lang3:3.14.0") {
                    because("org.apache.commons.lang3")
                }
            }"""
                .trimIndent()
        )
        p.moduleBuildFile("""plugins { id("org.hiero.gradle.module.library") }""")
        p.moduleInfoFile(
            """
            module org.hiero.product.module.a {
                requires transitive com.fasterxml.jackson.databind;
                requires org.apache.commons.lang3;
            }"""
                .trimIndent()
        )
        p.javaSourceFile(
            """
            package foo;
            
            import com.fasterxml.jackson.databind.ObjectMapper;
            
            public class ModuleA {
                private ObjectMapper om;
            }"""
                .trimIndent()
        )

        val result = p.failQualityCheck()

        assertThat(result.task(":module-a:checkModuleDirectivesScope")?.outcome)
            .isEqualTo(TaskOutcome.FAILED)
        assertThat(result.output)
            .contains(
                """
                  Please add the following requires directives:
                      requires com.fasterxml.jackson.databind;
                  
                  Please remove the following requires directives (or change to runtimeOnly):
                      requires org.apache.commons.lang3;
                      requires transitive com.fasterxml.jackson.databind;"""
                    .trimIndent()
            )
    }
}
