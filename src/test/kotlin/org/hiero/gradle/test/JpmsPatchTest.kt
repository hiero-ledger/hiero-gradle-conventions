// SPDX-License-Identifier: Apache-2.0
package org.hiero.gradle.test

import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.TaskOutcome
import org.hiero.gradle.test.fixtures.GradleProject
import org.junit.jupiter.api.Test

class JpmsPatchTest {

    @Test
    fun `all JPMS patching rules are working`() {
        val p = GradleProject().withMinimalStructure()
        val versionPatching =
            """
            configurations.all { attributes.attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, 21) }
            dependencies.components.withModule("com.goterl:resource-loader") { this.status = "release" }
            dependencies.components.withModule("com.goterl:lazysodium-java") { this.status = "release" }
            dependencies.components.all { if(listOf("alpha", "beta", "rc").any { id.version.lowercase().contains(it) }) status = "integration" }
            """
        p.aggregationBuildFile(
            """plugins { id("org.hiero.gradle.base.lifecycle") }
            dependencies { implementation(project(":module-a")) }
            $versionPatching
        """
                .trimIndent()
        )
        p.dependencyVersionsFile(
            """
            plugins { id("org.hiero.gradle.base.jpms-modules") }
            val modules = extraJavaModuleInfo.moduleSpecs.get().values.map { it.identifier }
            dependencies.constraints {
                modules.forEach {  api("${'$'}it:latest.release") }
                api("org.jetbrains:annotations:latest.release")
                api("org.mockito:mockito-core:latest.release")
                api("org.mockito:mockito-junit-jupiter:latest.release")
                api("org.hyperledger.besu:evm:24.3.3!!")
            }
        """
                .trimIndent()
        )
        p.moduleBuildFile(
            """
            plugins {
                id("java-library")
                id("org.hiero.gradle.base.jpms-modules")
            }
            val modules = extraJavaModuleInfo.moduleSpecs.get().values.map { it.moduleName }.distinct()
            file("src/main/java/module-info.java").writeText(
                "module org.example.module.a {\n${'$'}{modules.joinToString("") { "  requires ${'$'}it;\n"}}\n}")
            $versionPatching
            """
                .trimIndent()
        )
        p.help() // generate 'module-info.java' through code above

        val result = p.run("build")

        assertThat(result.task(":module-a:compileJava")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }
}
