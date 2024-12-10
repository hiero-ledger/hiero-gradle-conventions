// SPDX-License-Identifier: Apache-2.0
package org.hiero.gradle.test

import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.TaskOutcome
import org.hiero.gradle.extensions.CargoToolchain
import org.hiero.gradle.test.fixtures.GradleProject
import org.junit.jupiter.api.Test

class RustTasksTest {

    @Test
    fun `installRustToolchains installs all toolchains defined in CargoToolchain`() {
        val p = GradleProject().withMinimalStructure()
        val rustToolchainsDir = p.file("product/module-a/build/rust-toolchains/rustup/toolchains")
        p.moduleBuildFile("""plugins { id("org.hiero.gradle.feature.rust") }""")
        p.toolchainVersionsFile(
            """
            jdk=17.0.12
            rust=1.81.0
            cargo-zigbuild=0.19.5
            zig=0.13.0
            xwin=0.6.5
        """
                .trimIndent()
        )

        val result = p.run("installRustToolchains")

        assertThat(result.output).doesNotContain("warning: Force-skipping unavailable component")
        CargoToolchain.values().forEach { toolchain ->
            val toolchainDir =
                rustToolchainsDir
                    .walk()
                    .filter { it.name == toolchain.targetWithoutVersion() }
                    .single()
            assertThat(toolchainDir).isNotEmptyDirectory()
        }
        assertThat(result.task(":module-a:installRustToolchains")?.outcome)
            .isEqualTo(TaskOutcome.SUCCESS)
    }
}
