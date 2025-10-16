// SPDX-License-Identifier: Apache-2.0
package org.hiero.gradle.test

import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.TaskOutcome
import org.hiero.gradle.test.fixtures.GradleProject
import org.junit.jupiter.api.Test

class PluginVersionsTest {

    @Test
    fun `can access a plugin version in hiero-dependency-versions project`() {
        val p = GradleProject().withMinimalStructure()
        p.settingsFile(
            """
            plugins {
                id("org.hiero.gradle.build")
                id("com.hedera.pbj.pbj-compiler") version "0.8.9" apply false
            }
            
            rootProject.name = "test-project"
            
            javaModules { directory("product") { group = "org.example" } }    
        """
                .trimIndent()
        )
        p.dependencyVersionsFile(
            """
            plugins {
                id("org.hiero.gradle.base.lifecycle")
                id("org.hiero.gradle.base.jpms-modules")
            }
            val pbj = pluginVersions.version("com.hedera.pbj.pbj-compiler")
            dependencies.constraints {
                api("com.hedera.pbj:pbj-runtime:${'$'}pbj")
            }"""
                .trimIndent()
        )
        p.aggregationBuildFile(
            """
            dependencies { implementation(project(":module-a")) }
        """
                .trimIndent()
        )
        p.moduleBuildFile("""plugins { id("org.hiero.gradle.module.library") }""")
        p.moduleInfoFile(
            """
            module org.hiero.product.module.a {
                requires com.hedera.pbj.runtime;
            }"""
                .trimIndent()
        )

        val result = p.run("assemble")

        assertThat(result.task(":module-a:compileJava")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }

    @Test
    fun `fail with reasonable error if a plugin version is not available`() {
        val p = GradleProject().withMinimalStructure()
        p.dependencyVersionsFile(
            """
            plugins {
                id("org.hiero.gradle.base.lifecycle")
                id("org.hiero.gradle.base.jpms-modules")
                id("org.hiero.gradle.check.versions")
            }
            pluginVersions.version("com.hedera.pbj.pbj-compiler")"""
                .trimIndent()
        )

        val result = p.runAndFail("assemble")

        assertThat(result.output)
            .contains("Plugin not defined in settings.gradle.kts: com.hedera.pbj.pbj-compiler")
    }
}
