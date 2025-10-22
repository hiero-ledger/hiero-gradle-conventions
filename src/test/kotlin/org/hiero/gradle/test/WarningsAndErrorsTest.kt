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

        val result = p.runWithOldGradle()

        assertThat(result.output)
            .contains(
                """
                WARN: The hiero plugins are not fully compatible with the current Gradle version.
                 - Run: ./gradlew wrapper --gradle-version 9.1
        """
                    .trimIndent()
            )
    }

    @Test
    fun `warn if build cache is not enabled`() {
        val p = GradleProject()
        p.settingsFile("plugins { id(\"org.hiero.gradle.build\") }")

        val result = p.noTask()

        assertThat(result.output)
            .contains(
                """
                WARN: Build cache disabled
                 - Add org.gradle.caching=true to gradle.properties
        """
                    .trimIndent()
            )
    }

    @Test
    fun `warn if version is not pinned`() {
        val p = GradleProject()
        p.settingsFile("plugins { id(\"org.hiero.gradle.build\") }")

        val result = p.noTask()

        assertThat(result.output)
            .contains(
                """
                WARN: No version pinned in version.txt file (using: 0.1.0-SNAPSHOT)
                 - Run: ./gradlew versionAsSpecified -PnewVersion=0.1.0
        """
                    .trimIndent()
            )
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

        val result = p.qualityCheck()

        assertThat(result.output)
            .contains(
                """
                WARN: No 'jdk' version pinned (using: 17.0.16)
                 - Add jdk=17.0.16 to gradle/toolchain-versions.properties
        """
                    .trimIndent()
            )
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

        val result = p.qualityCheck()

        assertThat(result.output)
            .contains(
                """
                WARN: Gradle runs with Java 17.0.16. This project works best running with Java 17.0.99. 
                 - From commandline: change JAVA_HOME and/or PATH to point at Java 17.0.99 installation.
                 - From IntelliJ: change 'Gradle JVM' in 'Gradle Settings' to point at Java 17.0.99 installation.
        """
                    .trimIndent()
            )
    }
}
