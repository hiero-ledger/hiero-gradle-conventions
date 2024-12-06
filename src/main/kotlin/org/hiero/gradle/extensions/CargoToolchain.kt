// SPDX-License-Identifier: Apache-2.0
package org.hiero.gradle.extensions

enum class CargoToolchain(val platform: String, val target: String, val folder: String) {
    aarch64Darwin("darwin-aarch64", "aarch64-apple-darwin", "software/darwin/arm64"),
    aarch64Linux("linux-aarch64", "aarch64-unknown-linux-gnu.2.18", "software/linux/arm64"),
    x86Darwin("darwin-x86-64", "x86_64-apple-darwin", "software/darwin/amd64"),
    x86Linux("linux-x86-64", "x86_64-unknown-linux-gnu.2.18", "software/linux/amd64"),
    x86Windows("win32-x86-64-msvc", "x86_64-pc-windows-msvc", "software/windows/amd64")
}
