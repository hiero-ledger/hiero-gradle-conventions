// SPDX-License-Identifier: Apache-2.0
package org.hiero.gradle.test

import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.TaskOutcome
import org.hiero.gradle.test.fixtures.GradleProject
import org.junit.jupiter.api.Test

class ProtobufTest {

    @Test
    fun `task re-runs if protobuf plugin version changes`() {
        val testProto =
            """
            syntax = "proto3";
            package org.hiero.product.module.a;
            message Test {
              bytes test_id = 1;
            }
        """
                .trimIndent()
        val moduleInfo =
            """
            module org.hiero.product.module.a {
                requires com.google.protobuf;
            }
        """
                .trimIndent()
        val aggregation =
            """
            plugins {
                id("java")
                id("org.hiero.gradle.base.lifecycle")
            }
            dependencies { implementation(project(":module-a")) }
            """
                .trimIndent()

        val push = GradleProject().withMinimalStructure()
        val pull = GradleProject().withMinimalStructure()
        push.aggregationBuildFile(aggregation)
        pull.aggregationBuildFile(aggregation)
        push.settingsFile.appendText(
            """buildCache.local.directory = File("${push.file("build-cache").absolutePath}")"""
        )
        pull.settingsFile.appendText(
            """buildCache.local.directory = File("${push.file("build-cache").absolutePath}")"""
        )
        push.moduleBuildFile("""plugins { id("org.hiero.gradle.feature.protobuf") }""")
        pull.moduleBuildFile("""plugins { id("org.hiero.gradle.feature.protobuf") }""")

        push.moduleInfoFile(moduleInfo)
        pull.moduleInfoFile(moduleInfo)

        push.file("product/module-a/src/main/proto/test.proto", testProto)
        pull.file("product/module-a/src/main/proto/test.proto", testProto)

        push.dependencyVersionsFile(dependencyVersions("4.29.0"))
        pull.dependencyVersionsFile(dependencyVersions("4.29.3"))

        val pushResult = push.run("assemble --build-cache")
        val pullResult = pull.run("assemble --build-cache")

        // make sure second run is not FROM-CACHE
        assertThat(pushResult.task(":module-a:generateProto")?.outcome)
            .isEqualTo(TaskOutcome.SUCCESS)
        assertThat(pushResult.task(":module-a:compileJava")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
        assertThat(pullResult.task(":module-a:generateProto")?.outcome)
            .isEqualTo(TaskOutcome.SUCCESS)
        assertThat(pullResult.task(":module-a:compileJava")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }

    private fun dependencyVersions(protocVersion: String) =
        """
                plugins {
                    id("org.hiero.gradle.base.lifecycle")
                    id("org.hiero.gradle.base.jpms-modules")
                }
                dependencies.constraints {
                    api("com.google.protobuf:protobuf-java:4.29.0")
                    api("com.google.protobuf:protoc:$protocVersion")
                    api("io.grpc:protoc-gen-grpc-java:1.69.0")
                }"""
            .trimIndent()
}
