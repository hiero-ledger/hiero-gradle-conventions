// SPDX-License-Identifier: Apache-2.0
package org.hiero.gradle.test

import org.assertj.core.api.Assertions.assertThat
import org.hiero.gradle.test.fixtures.GradleProject
import org.junit.jupiter.api.Test

class WarningsAndErrorsTest {

    @Test
    fun `no warnings or errors in a well configured project`() {
        val p = GradleProject()
        p.withMinimalStructure()
        p.gradlePropertiesFile(
            """
            # SPDX-License-Identifier: Apache-2.0
            org.gradle.configuration-cache=true
            org.gradle.caching=true
            
        """
                .trimIndent()
        )

        val result = p.qualityCheck()

        assertThat(result.output).doesNotContain("WARN:")
    }

    @Test
    fun `warns if Gradle version is too low`() {
        val p = GradleProject()
        p.settingsFile("plugins { id(\"org.hiero.gradle.build\") }")

        p.runWithOldGradle()

        assertThat(p.problemsReport)
            .content()
            .contains("The hiero plugins are not fully compatible with the current Gradle version")
            .contains("Run: ./gradlew wrapper --gradle-version ")
    }

    @Test
    fun `warn if build cache is not enabled`() {
        val p = GradleProject()
        p.settingsFile("plugins { id(\"org.hiero.gradle.build\") }")

        p.noTask()

        assertThat(p.problemsReport)
            .content()
            .contains("Build Cache Disabled")
            .contains("Add org.gradle.caching=true to gradle.properties")
    }

    @Test
    fun `warn if version is not pinned`() {
        val p = GradleProject()
        p.settingsFile("plugins { id(\"org.hiero.gradle.build\") }")

        p.noTask()

        assertThat(p.problemsReport)
            .content()
            .contains("No version pinned in version.txt file (using: 0.1.0-SNAPSHOT)")
            .contains("Run: ./gradlew versionAsSpecified -PnewVersion=0.1.0")
    }

    @Test
    fun `project with modules - error if hiero-dependency-versions project is missing`() {
        val p = GradleProject()
        p.withMinimalStructure()
        p.dependencyVersions.parentFile.deleteRecursively()

        val result = p.failQualityCheck()

        assertThat(result.output)
            .contains("> Project with path ':hiero-dependency-versions' could not be found.")
    }

    @Test
    fun `project with modules - error if aggregation project is missing`() {
        val p = GradleProject()
        p.withMinimalStructure()
        p.aggregation.parentFile.deleteRecursively()

        val result = p.failQualityCheck()

        assertThat(result.output).contains("> Project with path ':aggregation' could not be found.")
    }

    @Test
    fun `project with modules - warn if Java version is not pinned`() {
        val p = GradleProject()
        p.withMinimalStructure()
        p.moduleBuildFile("""plugins { id("org.hiero.gradle.module.library") }""")
        p.toolchainVersionsFile.delete()

        p.qualityCheck()

        assertThat(p.problemsReport)
            .content()
            .contains("No 'jdk' version pinned (using: ")
            .contains("to gradle/toolchain-versions.properties")
    }

    @Test
    fun `project with modules - warn if pinned Java version is not used`() {
        val p = GradleProject()
        p.withMinimalStructure()
        p.moduleBuildFile("""plugins { id("org.hiero.gradle.module.library") }""")
        p.toolchainVersionsFile(
            """
            # SPDX-License-Identifier: Apache-2.0
            jdk=17.0.99
            
        """
                .trimIndent()
        )

        p.qualityCheck()

        assertThat(p.problemsReport)
            .content()
            .contains("This project works best running with Java 17.0.99")
            .contains("'Gradle JVM' in 'Gradle Settings'")
    }
}
