// SPDX-License-Identifier: Apache-2.0
package org.hiero.gradle.test

import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.TaskOutcome
import org.hiero.gradle.test.fixtures.GradleProject
import org.junit.jupiter.api.Test

class MavenCentralPublishTest {

    @Test
    fun `does not break with when using publish-artifactregistry plugin`() {
        val p = GradleProject().withMinimalStructure()
        p.withEnv(mapOf("NEXUS_USERNAME" to "foo", "NEXUS_PASSWORD" to "bar"))
        p.moduleBuildFile(
            """plugins { 
                id("org.hiero.gradle.module.library")
                id("org.hiero.gradle.feature.publish-artifactregistry")
            }
          """
                .trimIndent()
        )

        // We should not get: 'No staging repository with name sonatype created'
        p.run(
            "releaseMavenCentral -PpublishingPackageGroup=org.foo --dry-run --no-configuration-cache"
        )
    }

    @Test
    fun `attempts staging for non-snapshot releases`() {
        val p = GradleProject().withMinimalStructure()
        p.withEnv(mapOf("NEXUS_USERNAME" to "foo", "NEXUS_PASSWORD" to "bar"))
        p.moduleBuildFile("""plugins { id("org.hiero.gradle.module.library") }""")

        val result =
            p.runAndFail(
                "releaseMavenCentral -PpublishingPackageGroup=org.example --no-configuration-cache"
            )

        // Attempts to initialize staging but fails due to wrong credentials
        assertThat(result.output).contains("Failed to load staging profiles")
        assertThat(result.task(":initializeSonatypeStagingRepository")?.outcome)
            .isEqualTo(TaskOutcome.FAILED)
    }

    @Test
    fun `skips staging for snapshot releases`() {
        val p = GradleProject().withMinimalStructure()
        p.withEnv(mapOf("NEXUS_USERNAME" to "foo", "NEXUS_PASSWORD" to "bar"))
        p.moduleBuildFile("""plugins { id("org.hiero.gradle.module.library") }""")
        p.versionFile.writeText("1.0-SNAPSHOT")

        // Finishes successfully even though we did not provide credentials
        val result =
            p.runAndFail(
                "releaseMavenCentral -PpublishingPackageGroup=org.example --no-configuration-cache"
            )

        // Does actual publishing step of 'module-a' and only then fails due to wrong credentials
        assertThat(result.output)
            .contains("Failed to publish publication 'maven' to repository 'sonatype'")

        assertThat(result.task(":initializeSonatypeStagingRepository")?.outcome)
            .isEqualTo(TaskOutcome.SKIPPED)
        assertThat(result.task(":module-a:generateMetadataFileForMavenPublication")?.outcome)
            .isEqualTo(TaskOutcome.SUCCESS)
        assertThat(result.task(":module-a:generatePomFileForMavenPublication")?.outcome)
            .isEqualTo(TaskOutcome.SUCCESS)
        assertThat(result.task(":module-a:publishMavenPublicationToSonatypeRepository")?.outcome)
            .isEqualTo(TaskOutcome.FAILED)
    }

    @Test
    fun `publishes nothing if no modules exists for publishingPackageGroup`() {
        val p = GradleProject().withMinimalStructure()
        p.moduleBuildFile("""plugins { id("org.hiero.gradle.module.library") }""")
        p.versionFile.writeText("1.0-SNAPSHOT")

        // Finishes successfully even though we did not provide credentials, because
        // - We do not use a staging repository (SNAPSHOT)
        // - We do not actually publish any module (nothing in group 'wrong.group')
        val result =
            p.run(
                "releaseMavenCentral -PpublishingPackageGroup=wrong.group --no-configuration-cache"
            )

        assertThat(result.output).doesNotContain("warning: Force-skipping unavailable component")
        assertThat(result.task(":releaseMavenCentral")?.outcome).isEqualTo(TaskOutcome.UP_TO_DATE)
    }

    @Test
    fun `publishes only defined publishingPackageGroup`() {
        val p = GradleProject().withMinimalStructure()
        p.withEnv(mapOf("NEXUS_USERNAME" to "foo", "NEXUS_PASSWORD" to "bar"))

        // Modify some tasks to do nothing, because we cannot actually publish to Maven Central
        p.file(
            "build.gradle.kts",
            """
            tasks.initializeSonatypeStagingRepository { actions.clear(); doLast {} }
            tasks.closeSonatypeStagingRepository { stagingRepositoryId = ""; actions.clear(); doLast {} }
            tasks.releaseSonatypeStagingRepository { stagingRepositoryId = ""; actions.clear(); doLast {} }
        """
                .trimIndent()
        )
        p.moduleBuildFile(
            """
            plugins { id("org.hiero.gradle.module.library") }
            tasks.publishMavenPublicationToSonatypeRepository { actions.clear(); doLast {} }
            """
                .trimIndent()
        )

        // Add second product with a different group
        p.settingsFile.appendText(
            """javaModules { directory("product-b") { group = "org.foo" } }"""
        )
        p.file("product-b/module-b/src/main/java/module-info.java", "module org.foo.module.b {}")
        p.file(
            "product-b/module-b/build.gradle.kts",
            """
            plugins { id("org.hiero.gradle.module.library") }
            description = "Module B of Product B"
            """
                .trimIndent()
        )
        p.file(
            "product-b/module-b/src/main/java/org/foo/ModuleB.java",
            "package org.foo; class ModuleB {}"
        )

        val result =
            p.run(
                "releaseMavenCentral -PpublishingPackageGroup=org.example --no-configuration-cache"
            )

        assertThat(result.task(":initializeSonatypeStagingRepository")?.outcome)
            .isEqualTo(TaskOutcome.SUCCESS)
        assertThat(result.task(":module-a:publishMavenPublicationToSonatypeRepository")?.outcome)
            .isEqualTo(TaskOutcome.SUCCESS)
        assertThat(result.task(":module-b:publishMavenPublicationToSonatypeRepository")?.outcome)
            .isEqualTo(TaskOutcome.SKIPPED)
        assertThat(result.task(":initializeSonatypeStagingRepository")?.outcome)
            .isEqualTo(TaskOutcome.SUCCESS)
        assertThat(result.task(":closeSonatypeStagingRepository")?.outcome)
            .isEqualTo(TaskOutcome.SUCCESS)
        assertThat(result.task(":releaseSonatypeStagingRepository")?.outcome)
            .isEqualTo(TaskOutcome.SUCCESS)
    }

    @Test
    fun `does not perform auto-release of staging repository when publishTestRelease=true`() {
        val p = GradleProject().withMinimalStructure()
        p.withEnv(mapOf("NEXUS_USERNAME" to "foo", "NEXUS_PASSWORD" to "bar"))

        // Modify some tasks to do nothing, because we cannot actually publish to Maven Central
        p.file(
            "build.gradle.kts",
            """
            tasks.initializeSonatypeStagingRepository { actions.clear(); doLast {} }
            tasks.closeSonatypeStagingRepository { stagingRepositoryId = ""; actions.clear(); doLast {} }
        """
                .trimIndent()
        )
        p.moduleBuildFile(
            """
            plugins { id("org.hiero.gradle.module.library") }
            tasks.publishMavenPublicationToSonatypeRepository { actions.clear(); doLast {} }
            """
                .trimIndent()
        )
        val result =
            p.run(
                "releaseMavenCentral -PpublishingPackageGroup=org.example --no-configuration-cache -PpublishTestRelease=true"
            )

        assertThat(result.task(":initializeSonatypeStagingRepository")?.outcome)
            .isEqualTo(TaskOutcome.SUCCESS)
        assertThat(result.task(":module-a:publishMavenPublicationToSonatypeRepository")?.outcome)
            .isEqualTo(TaskOutcome.SUCCESS)
        assertThat(result.task(":initializeSonatypeStagingRepository")?.outcome)
            .isEqualTo(TaskOutcome.SUCCESS)
        assertThat(result.task(":closeSonatypeStagingRepository")?.outcome)
            .isEqualTo(TaskOutcome.SUCCESS)
        assertThat(result.task(":releaseSonatypeStagingRepository")).isNull() // not in task graph
    }

    @Test
    fun `can use tasks task without issue if there are no publishing credentials`() {
        val p = GradleProject().withMinimalStructure()
        p.moduleBuildFile("""plugins { id("org.hiero.gradle.module.library") }""")

        val result = p.run("tasks")

        assertThat(result.task(":tasks")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }
}
