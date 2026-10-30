// SPDX-License-Identifier: Apache-2.0
import org.hiero.gradle.extensions.CargoToolchain

plugins { id("org.gradlex.java-module-packaging") }

javaModulePackaging {
    CargoToolchain.entries.forEach { toolchain ->
        target(toolchain.name) {
            operatingSystem = toolchain.os
            architecture = toolchain.arch
        }
    }
}
