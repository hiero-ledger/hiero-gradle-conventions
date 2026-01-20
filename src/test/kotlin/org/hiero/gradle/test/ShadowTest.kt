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
            }
            """
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

        val result = p.run("shadowJar")

        assertThat(result.task(":module-a:shadowJar")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }

    @Test
    fun `shadowJar does not run as part of assemble when combined with application plugin`() {
        val p = GradleProject().withMinimalStructure()
        p.moduleBuildFile(
            """
            plugins {
                id("org.hiero.gradle.feature.shadow")
                id("application")
            }
            application {
                mainClass = "org.hiero.product.module.a.ModuleA"
            }
            """
                .trimIndent()
        )

        val result = p.run("assemble")

        assertThat(result.task(":module-a:shadowJar")).isNull()
    }

    @Test
    fun `shadowJar merges service files`() {
        val p = GradleProject().withMinimalStructure()
        p.dependencyVersionsFile(
            """
            dependencies.constraints {
                api("com.fasterxml.jackson.core:jackson-core:2.20.0")
                api("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.20.0")
            }
            """
                .trimIndent()
        )
        p.moduleInfoFile(
            """
            module org.hiero.product.module.a {
                requires com.fasterxml.jackson.core;
                requires com.fasterxml.jackson.dataformat.yaml;
            }
            """
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
            // unzip result of shadowJar for assertions in test
            tasks.register<Copy>("unzipShadowJar") {
                from(zipTree(tasks.shadowJar.flatMap { it.archiveFile }))
                into(layout.buildDirectory.dir("shadowContent"))
            }
            """
                .trimIndent()
        )

        val result = p.run(":module-a:unzipShadowJar")

        assertThat(result.task(":module-a:shadowJar")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
        assertThat(
                p.file(
                    "product/module-a/build/shadowContent/META-INF/services/com.fasterxml.jackson.core.JsonFactory"
                )
            )
            .hasContent(
                """
                com.fasterxml.jackson.dataformat.yaml.YAMLFactory
                com.fasterxml.jackson.core.JsonFactory
                """
                    .trimIndent()
            )
    }

    @Test
    fun `fails for duplicated files`() {
        val p = GradleProject().withMinimalStructure()
        p.file("product/module-a/src/main/resources/org/hiero/product/test.txt", "H")
        p.moduleBuildFile(
            """
            plugins { id("org.hiero.gradle.feature.shadow") }

            tasks.shadowJar {
                // include the same file from two different places
                from(tasks.processResources)
                from("src/main/resources")
            }
            """
                .trimIndent()
        )

        val result = p.runAndFail("shadowJar")
        assertThat(result.output).contains("> Cannot copy file ")
    }
}
