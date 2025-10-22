// SPDX-License-Identifier: Apache-2.0
import org.hiero.gradle.environment.EnvAccess
import org.hiero.gradle.extensions.CargoToolchain
import org.hiero.gradle.tasks.RustToolchainInstallTask

val os =
    System.getProperty("os.name").lowercase().let {
        if (it.contains("windows")) {
            "windows"
        } else if (it.contains("mac")) {
            "macos"
        } else {
            "linux"
        }
    }

fun errorMissingVersion(key: String) = provider {
    throw RuntimeException("No '$key' version defined in 'gradle/toolchain-versions.properties'")
}

tasks.register<RustToolchainInstallTask>("installRustToolchains") {
    description = "Installs Rust and toolchain components required for cross-compilation"

    val versions = EnvAccess.toolchainVersions(layout.projectDirectory, providers, objects)
    rustVersion.convention(versions.getting("rust").orElse(errorMissingVersion("rust")))
    cargoZigbuildVersion.convention(
        versions.getting("cargo-zigbuild").orElse(errorMissingVersion("cargo-zigbuild"))
    )
    zigVersion.convention(versions.getting("zig").orElse(errorMissingVersion("zig")))
    xwinVersion.convention(versions.getting("xwin").orElse(errorMissingVersion("xwin")))

    // Track host system as input as the task output differs between operating systems
    hostOperatingSystem.set(os)
    hostArchitecture.set(System.getProperty("os.arch"))

    toolchains.convention(CargoToolchain.entries)
    destinationDirectory.convention(layout.buildDirectory.dir("rust-toolchains"))

    if (EnvAccess.isCiServer(providers)) {
        // Disable, due to 'Could not load entry ... from remote build cache: Read timed out'
        outputs.cacheIf { false }
    }
}
