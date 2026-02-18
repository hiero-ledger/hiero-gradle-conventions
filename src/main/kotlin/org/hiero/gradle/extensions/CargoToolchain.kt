// SPDX-License-Identifier: Apache-2.0
package org.hiero.gradle.extensions

import org.gradle.nativeplatform.MachineArchitecture.ARM64
import org.gradle.nativeplatform.MachineArchitecture.X86_64
import org.gradle.nativeplatform.OperatingSystemFamily.LINUX
import org.gradle.nativeplatform.OperatingSystemFamily.MACOS
import org.gradle.nativeplatform.OperatingSystemFamily.WINDOWS

@Suppress("UnstableApiUsage")
enum class CargoToolchain(
    val platform: String,
    val os: String,
    val arch: String,
    val target: String,
    val folder: String,
) {
    aarch64Darwin("darwin-aarch64", MACOS, ARM64, "aarch64-apple-darwin", "darwin/arm64"),
    aarch64Linux("linux-aarch64", LINUX, ARM64, "aarch64-unknown-linux-gnu.2.18", "linux/arm64"),
    x86Darwin("darwin-x86-64", MACOS, X86_64, "x86_64-apple-darwin", "darwin/amd64"),
    x86Linux("linux-x86-64", LINUX, X86_64, "x86_64-unknown-linux-gnu.2.18", "linux/amd64"),
    x86Windows("win32-x86-64-msvc", WINDOWS, X86_64, "x86_64-pc-windows-msvc", "windows/amd64");

    /** Returns 'target', but without version suffix like '.2.18' if there is one. */
    fun targetWithoutVersion() = target.substringBefore(".")
}
