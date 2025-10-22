// SPDX-License-Identifier: Apache-2.0
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
        assertThat(result.output).contains("No description found in")
        assertThat(result.output)
            .contains("product/module-a/build/publications/maven/pom-default.xml")
    }

    @Test
    fun `qualityCheck fails for wrong dependency scopes`() {
        val p = GradleProject().withMinimalStructure()
        p.dependencyVersionsFile(
            """
            dependencies.constraints {
                api("com.fasterxml.jackson.core:jackson-databind:2.16.0") {
                    because("com.fasterxml.jackson.databind")
                }
                api("org.apache.commons:commons-lang3:3.14.0") { because("org.apache.commons.lang3") }
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

    @Test
    fun `qualityCheck detects inconsistend and unused versions`() {
        val p = GradleProject().withMinimalStructure()
        p.dependencyVersionsFile(
            """
            plugins {
                id("org.hiero.gradle.base.lifecycle")
                id("org.hiero.gradle.base.jpms-modules")
                id("org.hiero.gradle.check.versions")
            }
            dependencies.constraints {
                api("com.fasterxml.jackson.core:jackson-databind:2.16.0")
                api("com.fasterxml.jackson.core:jackson-core:2.15.0")
                api("org.apache.commons:commons-lang3:3.14.0")
                api("com.google.protobuf:protoc:4.31.1")
            }
            tasks.checkVersionConsistency {
                excludes.add("com.google.protobuf:protoc") // protoc tool
            }"""
                .trimIndent()
        )
        p.aggregationBuildFile(
            """
            plugins { id("java") }
            dependencies { implementation(project(":module-a")) }
        """
                .trimIndent()
        )
        p.moduleBuildFile("""plugins { id("org.hiero.gradle.module.library") }""")
        p.moduleInfoFile(
            """
            module org.hiero.product.module.a {
                requires com.fasterxml.jackson.databind;
            }"""
                .trimIndent()
        )
        p.javaSourceFile(
            """
            package foo;
            import com.fasterxml.jackson.databind.ObjectMapper;
            public class ModuleA { private ObjectMapper om; }"""
                .trimIndent()
        )

        val result = p.failQualityCheck()

        assertThat(result.task(":hiero-dependency-versions:checkVersionConsistency")?.outcome)
            .isEqualTo(TaskOutcome.FAILED)
        assertThat(result.output)
            .contains(
                """
                > Wrong version: com.fasterxml.jackson.core:jackson-core (declared=2.15.0; used=2.16.0)
                  Not used: org.apache.commons:commons-lang3:3.14.0
                """
                    .trimIndent()
            )
    }
}
