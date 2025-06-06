// SPDX-License-Identifier: Apache-2.0
package org.hiero.gradle.extensions

enum class CargoToolchain(val platform: String, val target: String, val folder: String) {
    aarch64Darwin("darwin-aarch64", "aarch64-apple-darwin", "darwin/arm64"),
    aarch64Linux("linux-aarch64", "aarch64-unknown-linux-gnu.2.18", "linux/arm64"),
    x86Darwin("darwin-x86-64", "x86_64-apple-darwin", "darwin/amd64"),
    x86Linux("linux-x86-64", "x86_64-unknown-linux-gnu.2.18", "linux/amd64"),
    x86Windows("win32-x86-64-msvc", "x86_64-pc-windows-msvc", "windows/amd64");

    /** Returns 'target', but without version suffix like '.2.18' if there is one. */
    fun targetWithoutVersion() = target.substringBefore(".")
}
