// SPDX-License-Identifier: Apache-2.0
package org.hiero.gradle.test

import org.assertj.core.api.Assertions.assertThat
import org.hiero.gradle.test.fixtures.GradleProject
import org.junit.jupiter.api.Test

class PublishDependencyConstraintsTest {

    @Test
    fun `can publish constraints from platform`() {
        val p = GradleProject().withMinimalStructure()
        p.dependencyVersionsFile(
            """
            plugins {
                id("org.hiero.gradle.base.lifecycle")
                id("org.hiero.gradle.base.jpms-modules")
            }
            dependencies.constraints {
                api("io.grpc:grpc-netty:1.69.0")
            }"""
                .trimIndent()
        )
        p.moduleBuildFile(
            """
            plugins { 
                id("org.hiero.gradle.module.library")
                id("org.hiero.gradle.feature.publish-dependency-constraints")
            }
            dependencies {
                publishDependencyConstraint("io.grpc:grpc-netty")
            }
          """
                .trimMargin()
        )

        p.run("generateMetadataFileForMavenPublication")

        assertThat(p.file("product/module-a/build/publications/maven/module.json"))
            .content()
            .contains(
                """
            |      "dependencyConstraints": [
            |        {
            |          "group": "io.grpc",
            |          "module": "grpc-netty",
            |          "version": {
            |            "requires": "1.69.0"
            |          }
            |        }
            |      ],"""
                    .trimMargin()
            )
    }
}
