// SPDX-License-Identifier: Apache-2.0
package org.hiero.gradle.test

import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.TaskOutcome
import org.hiero.gradle.test.fixtures.GradleProject
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class BenchmarkTest {

    lateinit var p: GradleProject

    @BeforeEach
    fun setup() {
        p = GradleProject().withMinimalStructure()
        p.moduleBuildFile(
            """
            plugins { id("org.hiero.gradle.feature.benchmark") }

            tasks.jmh {
                fork = 1
                warmupIterations = 0
                iterations = 1
            }
            """
                .trimIndent()
        )
        p.file(
            "product/module-a/src/jmh/java/org/hiero/product/SampleBenchmark.java",
            """
            package org.hiero.product;

            import org.openjdk.jmh.annotations.Benchmark;
            import org.openjdk.jmh.annotations.Scope;
            import org.openjdk.jmh.annotations.State;
            import org.openjdk.jmh.infra.Blackhole;

            @State(Scope.Benchmark)
            public class SampleBenchmark {
                @Benchmark
                public void example(Blackhole bh) { }
            }
            """
                .trimIndent(),
        )
    }

    @Test
    fun `can use jmh task`() {
        val result = p.run("jmh")

        assertThat(result.task(":module-a:jmhJar")).isNull()
        assertThat(result.task(":module-a:jmhJarWithMergedServiceFiles")?.outcome)
            .isEqualTo(TaskOutcome.SUCCESS)
    }

    @Test
    fun `service files in jmh jar are merged`() {
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
                id("org.hiero.gradle.feature.benchmark")
            }
            application {
                mainClass = "org.hiero.product.module.a.ModuleA"
            }
            // unzip result of shadowJar for assertions in test
            tasks.register<Copy>("unzipJmhJar") {
                from(zipTree(tasks.jmhJarWithMergedServiceFiles.flatMap { it.archiveFile }))
                into(layout.buildDirectory.dir("jmhContent"))
            }
            """
                .trimIndent()
        )

        val result = p.run(":module-a:unzipJmhJar")
        assertThat(result.task(":module-a:jmhJarWithMergedServiceFiles")?.outcome)
            .isEqualTo(TaskOutcome.SUCCESS)
        assertThat(
                p.file(
                    "product/module-a/build/jmhContent/META-INF/services/com.fasterxml.jackson.core.JsonFactory"
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
}
