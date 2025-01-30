// SPDX-License-Identifier: Apache-2.0
import org.hiero.gradle.extensions.CargoExtension
import org.hiero.gradle.extensions.CargoToolchain

plugins { id("java") }

val cargo = project.extensions.create<CargoExtension>("cargo")

cargo.targets(*CargoToolchain.values())
