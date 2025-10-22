// SPDX-License-Identifier: Apache-2.0
package org.hiero.gradle.test

import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.TaskOutcome
import org.hiero.gradle.test.fixtures.GradleProject
import org.junit.jupiter.api.Test

class LegacyClasspathTest {

    @Test
    fun `can build a classpath-based application`() {
        val p = GradleProject().withMinimalStructure()
        p.moduleInfo.delete()
        // explicitly register Module without module-info
        p.settingsFile.appendText("""javaModules { module("product/module-a") }""")
        p.dependencyVersionsFile(
            """
            dependencies { api(platform("com.google.cloud:libraries-bom:26.49.0")) }
            """
                .trimIndent()
        )
        p.aggregationBuildFile(
            """
            plugins { id("java") }
            dependencies { implementation(project(":module-a")) }
        """
                .trimIndent()
        )
        p.file(
            "gradle/modules.properties",
            """
            # Jars that are not yet modules
            com.google.api.gax=com.google.api:gax
            com.google.auth.oauth2=com.google.auth:google-auth-library-oauth2-http
            com.google.cloud.core=com.google.cloud:google-cloud-core
            com.google.cloud.storage=com.google.cloud:google-cloud-storage
        """
                .trimIndent(),
        )
        p.moduleBuildFile(
            """
            plugins {
                id("org.hiero.gradle.module.application")
                id("org.hiero.gradle.feature.legacy-classpath")
            }
            mainModuleInfo {  
                requires("com.google.api.gax")
                requires("com.google.auth.oauth2")
                requires("com.google.cloud.core")
                requires("com.google.cloud.storage")
            }
        """
                .trimIndent()
        )

        val result = p.run("assemble")

        assertThat(result.task(":module-a:assemble")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }
}
