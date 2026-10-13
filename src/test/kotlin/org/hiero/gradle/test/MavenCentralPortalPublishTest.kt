// SPDX-License-Identifier: Apache-2.0
package org.hiero.gradle.test

import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.TaskOutcome
import org.hiero.gradle.test.fixtures.GradleProject
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

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
        val result = p.runAndFail("nmcpPublishAggregationToCentralPortal")

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
                "> Cannot upload deployment to maven central: (HTTP error 401: '{\"error\":{\"message\":\"Invalid token\"}}')"
            )
        assertThat(result.task(":aggregation:nmcpZipAggregation")?.outcome)
            .isEqualTo(TaskOutcome.SUCCESS)
        assertThat(result.task(":aggregation:nmcpPublishAggregationToCentralPortal")?.outcome)
            .isEqualTo(TaskOutcome.FAILED)
    }

    @Test
    fun `publishes each module directly for snapshot releases`() {
        p.versionFile.writeText("1.0-SNAPSHOT")

        // Required to make sure NMCP downloaded all its dependencies before running with --offline
        p.run("cleanupDirectory")

        // Finishes successfully even though we did not provide credentials
        val result = p.runAndFail("nmcpPublishAggregationToCentralPortal --offline --continue")

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

    @ParameterizedTest
    @ValueSource(strings = ["publishPrivateRelease", "publishTestRelease"])
    fun `does not perform auto-release when private release is requested`(property: String) {
        // Modify some tasks to do nothing, because we cannot actually publish to Maven Central
        p.aggregationBuildFile(
            """
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
            """
        )
        val result = p.run("nmcpPublishAggregationToCentralPortal -P$property=true")

        assertThat(result.task(":module-a:publishMavenPublicationToNmcpRepository")?.outcome)
            .isEqualTo(TaskOutcome.SUCCESS)
        assertThat(result.task(":aggregation:nmcpZipAggregation")?.outcome)
            .isEqualTo(TaskOutcome.SUCCESS)
        assertThat(result.output).contains("publishingType=USER_MANAGED")
    }

    @Test
    fun `performs auto-release by default`() {
        p.aggregationBuildFile(
            """
            dependencies { published(project(":module-a")) }
            tasks.nmcpPublishAggregationToCentralPortal {
                actions.clear()
                doLast { println("publishingType=" + inputs.properties["publishingType"]) }
            }
        """
        )
        val result = p.run("nmcpPublishAggregationToCentralPortal")

        assertThat(result.output).contains("publishingType=AUTOMATIC")
    }

    @Test
    fun `attempts to make an existing private release public`() {
        val result = p.runAndFail("nmcpPublishDeployment -PnmcpDeploymentId=1234")

        // Does not build or upload anything, but fails to publish due to bad credentials
        assertThat(result.output)
            .contains("Publishing previously uploaded deployment bundle '1234'")
        assertThat(result.output).contains("Cannot publish deployment '1234' to maven central")
        assertThat(result.task(":aggregation:nmcpZipAggregation")).isNull()
        assertThat(result.task(":aggregation:nmcpPublishDeployment")?.outcome)
            .isEqualTo(TaskOutcome.FAILED)
    }

    @Test
    fun `making a private release public requires a deployment id`() {
        val result = p.runAndFail("nmcpPublishDeployment")

        assertThat(result.output).contains("Nmcp: deploymentId is missing")
    }

    @Test
    fun `can use tasks task without issue if there are no publishing credentials`() {
        val result = p.run("tasks")

        assertThat(result.task(":tasks")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }
}
