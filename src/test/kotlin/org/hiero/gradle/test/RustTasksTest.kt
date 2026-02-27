// SPDX-License-Identifier: Apache-2.0
package org.hiero.gradle.test

import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.TaskOutcome
import org.hiero.gradle.extensions.CargoToolchain
import org.hiero.gradle.test.fixtures.GradleProject
import org.junit.jupiter.api.Test

class RustTasksTest {

    private val toolchainVersions =
        """
        jdk=17.0.12
        rust=1.81.0
        cargo-zigbuild=0.19.5
        zig=0.13.0
        xwin=0.6.5
        """
            .trimIndent()

    private val cargoToml =
        """
        [package]
        name = "module-a"
        version = "0.0.1"
        edition = "2021"
        [lib]
        path = "src/main/rust/lib.rs"
        crate-type = ["cdylib"]
        """
            .trimIndent()

    // Test asserts multiple things in one method as installing the toolchains is expensive.
    @Test
    fun `rust toolchain installation and caching works`() {
        val push = GradleProject().withMinimalStructure()
        val pull = GradleProject().withMinimalStructure()
        push.settingsFile.appendText(
            """buildCache.local.directory = File("${push.file("build-cache").absolutePath}")"""
        )
        pull.settingsFile.appendText(
            """buildCache.local.directory = File("${push.file("build-cache").absolutePath}")"""
        )
        push.moduleBuildFile("""plugins { id("org.hiero.gradle.feature.rust") }""")
        pull.moduleBuildFile("""plugins { id("org.hiero.gradle.feature.rust") }""")
        push.toolchainVersionsFile(toolchainVersions)
        pull.toolchainVersionsFile(toolchainVersions)
        push.file("product/module-a/src/main/rust/lib.rs", "pub fn public_api() {}")
        pull.file("product/module-a/src/main/rust/lib.rs", "pub fn public_api() {}")
        push.file("product/module-a/Cargo.toml", cargoToml)
        pull.file("product/module-a/Cargo.toml", cargoToml)

        val rustToolchainsDir = push.file("build/rust-toolchains/rustup/toolchains")

        val pushResult = push.run("assemble -PpackageAllTargets=true --build-cache")
        val pullResult =
            pull.run(
                "assemble -PpackageAllTargets=true --build-cache -PskipInstallRustToolchains=true"
            )

        // installRustToolchains installs all toolchains defined in CargoToolchain
        assertThat(pushResult.output)
            .doesNotContain("warning: Force-skipping unavailable component")
        CargoToolchain.values().forEach { toolchain ->
            val toolchainDir =
                rustToolchainsDir
                    .walk()
                    .filter { it.name == toolchain.targetWithoutVersion() }
                    .single()
            assertThat(toolchainDir).isNotEmptyDirectory()
        }

        // rust build results are taken FROM-CACHE and installRustToolchains can be skipped
        assertThat(pushResult.task(":installRustToolchains")?.outcome)
            .isEqualTo(TaskOutcome.SUCCESS)
        assertThat(pullResult.task(":installRustToolchains")).isNull()
        assertThat(pushResult.task(":module-a:cargoBuildAarch64Darwin")?.outcome)
            .isEqualTo(TaskOutcome.SUCCESS)
        assertThat(pullResult.task(":module-a:cargoBuildAarch64Darwin")?.outcome)
            .isEqualTo(TaskOutcome.FROM_CACHE)
        assertThat(pushResult.task(":module-a:cargoBuildAarch64Linux")?.outcome)
            .isEqualTo(TaskOutcome.SUCCESS)
        assertThat(pullResult.task(":module-a:cargoBuildAarch64Linux")?.outcome)
            .isEqualTo(TaskOutcome.FROM_CACHE)
        assertThat(pushResult.task(":module-a:cargoBuildX86Darwin")?.outcome)
            .isEqualTo(TaskOutcome.SUCCESS)
        assertThat(pullResult.task(":module-a:cargoBuildX86Darwin")?.outcome)
            .isEqualTo(TaskOutcome.FROM_CACHE)
        assertThat(pushResult.task(":module-a:cargoBuildX86Linux")?.outcome)
            .isEqualTo(TaskOutcome.SUCCESS)
        assertThat(pullResult.task(":module-a:cargoBuildX86Linux")?.outcome)
            .isEqualTo(TaskOutcome.FROM_CACHE)
        assertThat(pushResult.task(":module-a:cargoBuildX86Windows")?.outcome)
            .isEqualTo(TaskOutcome.SUCCESS)
        assertThat(pullResult.task(":module-a:cargoBuildX86Windows")?.outcome)
            .isEqualTo(TaskOutcome.FROM_CACHE)
    }
}
