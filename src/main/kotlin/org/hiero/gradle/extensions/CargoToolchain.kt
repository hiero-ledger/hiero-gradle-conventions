/*
 * Copyright (C) 2024 Hiero a Series of LF Projects, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.hiero.gradle.extensions

enum class CargoToolchain(val platform: String, val target: String, val folder: String) {
    aarch64Darwin("darwin-aarch64", "aarch64-apple-darwin", "software/darwin/arm64"),
    aarch64Linux("linux-aarch64", "aarch64-unknown-linux-gnu", "software/linux/arm64"),
    x86Darwin("darwin-x86-64", "x86_64-apple-darwin", "software/darwin/amd64"),
    x86Linux("linux-x86-64", "x86_64-unknown-linux-gnu", "software/linux/amd64"),
    x86Windows("win32-x86-64-msvc", "x86_64-pc-windows-msvc", "software/windows/amd64")
}
