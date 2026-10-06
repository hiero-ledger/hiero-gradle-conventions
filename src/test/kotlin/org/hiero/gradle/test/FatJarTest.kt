// SPDX-License-Identifier: Apache-2.0
package org.hiero.gradle.test

import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.TaskOutcome
import org.hiero.gradle.test.fixtures.GradleProject
import org.junit.jupiter.api.Test

class FatJarTest {

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
                id("org.hiero.gradle.feature.packaging")
            }
            application {
                mainClass = "org.hiero.product.module.a.ModuleA"
            }
            """
                .trimIndent()
        )

        val result = p.run("fatModuleJar")

        assertThat(result.task(":module-a:fatModuleJar")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }

    @Test
    fun `fatModuleJar does not run as part of assemble when combined with application plugin`() {
        val p = GradleProject().withMinimalStructure()
        p.moduleBuildFile(
            """
            plugins {
                id("org.hiero.gradle.feature.packaging")
                id("application")
            }
            application {
                mainClass = "org.hiero.product.module.a.ModuleA"
            }
            """
                .trimIndent()
        )

        val result = p.run("assemble")

        assertThat(result.task(":module-a:fatModuleJar")).isNull()
    }

    @Test
    fun `service files with same name are all included in fat jar`() {
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
                id("org.hiero.gradle.feature.packaging")
            }
            application {
                mainClass = "org.hiero.product.module.a.ModuleA"
            }
            // unzip result of fatModuleJar for assertions in test
            tasks.register<Copy>("unzipfatModuleJar") {
                from(zipTree(tasks.fatModuleJar.flatMap { it.archiveFile }))
                into(layout.buildDirectory.dir("fatJarContent"))
            }
            """
                .trimIndent()
        )

        val result = p.run(":module-a:unzipfatModuleJar")

        assertThat(result.task(":module-a:fatModuleJar")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
        assertThat(
                p.file(
                    "product/module-a/build/fatJarContent/modulepath/jackson-core-2.20.0/META-INF/services/com.fasterxml.jackson.core.JsonFactory"
                )
            )
            .hasContent(
                """
                com.fasterxml.jackson.core.JsonFactory
                """
                    .trimIndent()
            )
        assertThat(
                p.file(
                    "product/module-a/build/fatJarContent/modulepath/jackson-dataformat-yaml-2.20.0/META-INF/services/com.fasterxml.jackson.core.JsonFactory"
                )
            )
            .hasContent(
                """
                com.fasterxml.jackson.dataformat.yaml.YAMLFactory
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
            plugins {
                id("org.hiero.gradle.feature.packaging")
                id("application")
            }
            application {
                mainClass = "org.hiero.product.module.a.ModuleA"
            }

            tasks.fatModuleJar {
                // include the same file from two different places
                from(tasks.processResources)
                from("src/main/resources")
            }
            """
                .trimIndent()
        )

        val result = p.runAndFail("fatModuleJar")
        assertThat(result.output).contains("Entry org/hiero/product/test.txt is a duplicate")
    }
}
