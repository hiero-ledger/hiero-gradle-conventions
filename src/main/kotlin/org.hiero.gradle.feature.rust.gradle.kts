// SPDX-License-Identifier: Apache-2.0
import org.hiero.gradle.extensions.CargoExtension
import org.hiero.gradle.extensions.CargoToolchain

plugins { id("java") }

val cargo = project.extensions.create<CargoExtension>("cargo")

cargo.targets(*CargoToolchain.values())

if (System.getenv().containsKey("CI")) {
    // Disable, due to 'Could not load entry ... from remote build cache: Read timed out'
    tasks.named("installRustToolchains") { outputs.cacheIf { false } }
}
