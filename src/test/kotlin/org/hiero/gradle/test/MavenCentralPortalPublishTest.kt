// SPDX-License-Identifier: Apache-2.0
package org.hiero.gradle.test

import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.TaskOutcome
import org.hiero.gradle.test.fixtures.GradleProject
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class MavenCentralPortalPublishTest {

    lateinit var p: GradleProject

    @BeforeEach
    fun setup() {
        p = GradleProject().withMinimalStructure()
        p.withEnv(mapOf("NEXUS_USERNAME" to "foo", "NEXUS_PASSWORD" to "bar"))
        p.aggregationBuildFile(
            """
            plugins { id("org.hiero.gradle.feature.publish-maven-central-aggregation") }
            dependencies { published(project(":module-a")) }
        """
        )
        p.moduleBuildFile("""plugins { id("org.hiero.gradle.module.library") }""")
    }

    @Test
    fun `attempts to upload a single archive for non-snapshot releases`() {
        val result = p.runAndFail("publishAggregationToCentralPortal")

        // Creates the publication zip, but fails to upload due to bad credentials
        assertThat(
                p.file("product/module-a/build/nmcp/m2/org/example/module-a/1.0/module-a-1.0.jar")
            )
            .exists()
        assertThat(
                p.file(
                    "product/module-a/build/nmcp/m2/org/example/module-a/1.0/module-a-1.0.module"
                )
            )
            .exists()
        assertThat(result.output)
            .contains(
                "> Cannot deploy to maven central (status='401'): {\"error\":{\"message\":\"Invalid token\"}}"
            )
        assertThat(result.task(":aggregation:nmcpZipAggregation")?.outcome)
            .isEqualTo(TaskOutcome.SUCCESS)
        assertThat(result.task(":aggregation:nmcpPublishAggregationToCentralPortal")?.outcome)
            .isEqualTo(TaskOutcome.FAILED)
    }

    @Test
    fun `publishes each module directly for snapshot releases`() {
        p.versionFile.writeText("1.0-SNAPSHOT")

        // Finishes successfully even though we did not provide credentials
        val result = p.runAndFail("publishAggregationToCentralPortal --offline")

        // Does attempt actual publishing step of 'module-a' (fails only due to --offline)
        assertThat(p.dir("product/module-a/build/nmcp/m2")).doesNotExist()
        assertThat(result.output)
            .contains(
                "> No cached resource 'https://central.sonatype.com/repository/maven-snapshots/"
            )
        assertThat(result.task(":module-a:generateMetadataFileForMavenPublication")?.outcome)
            .isEqualTo(TaskOutcome.SUCCESS)
        assertThat(result.task(":module-a:generatePomFileForMavenPublication")?.outcome)
            .isEqualTo(TaskOutcome.SUCCESS)
        assertThat(result.task(":module-a:publishMavenPublicationToNmcpRepository")?.outcome)
            .isEqualTo(TaskOutcome.FAILED)
    }

    @Test
    fun `does not perform auto-release when publishTestRelease=true`() {
        // Modify some tasks to do nothing, because we cannot actually publish to Maven Central
        p.aggregationBuildFile(
            """
            plugins { id("org.hiero.gradle.feature.publish-maven-central-aggregation") }
            dependencies { published(project(":module-a")) }
            tasks.nmcpPublishAggregationToCentralPortal {
                actions.clear()
                doLast { println("publishingType=" + inputs.properties["publishingType"]) }
            }
        """
        )
        p.moduleBuildFile(
            """
            plugins { id("org.hiero.gradle.module.library") }
            tasks.publishMavenPublicationToSonatypeRepository { actions.clear(); doLast {} }
            """
        )
        val result = p.run("publishAggregationToCentralPortal -PpublishTestRelease=true")

        assertThat(result.task(":module-a:publishMavenPublicationToNmcpRepository")?.outcome)
            .isEqualTo(TaskOutcome.SUCCESS)
        assertThat(result.task(":aggregation:nmcpZipAggregation")?.outcome)
            .isEqualTo(TaskOutcome.SUCCESS)
        assertThat(result.output).contains("publishingType=USER_MANAGED")
    }

    @Test
    fun `can use tasks task without issue if there are no publishing credentials`() {
        val result = p.run("tasks")

        assertThat(result.task(":tasks")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }
}
