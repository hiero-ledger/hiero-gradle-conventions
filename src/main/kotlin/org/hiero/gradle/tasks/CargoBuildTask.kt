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

package org.hiero.gradle.tasks

import java.io.File
import javax.inject.Inject
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.internal.file.FileOperations
import org.gradle.api.logging.LogLevel
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import org.hiero.gradle.extensions.CargoToolchain

@CacheableTask
abstract class CargoBuildTask : DefaultTask() {
    @get:Input abstract val libname: Property<String>

    @get:Input abstract val release: Property<Boolean>

    @get:Input abstract val toolchain: Property<CargoToolchain>

    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val cargoToml: RegularFileProperty

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.NAME_ONLY)
    abstract val sourcesDirectory: DirectoryProperty

    @get:OutputDirectory abstract val destinationDirectory: DirectoryProperty

    @get:Internal abstract val cargoBin: Property<String>

    @get:Internal abstract val xwinFolder: Property<String>

    @get:Inject protected abstract val exec: ExecOperations

    @get:Inject protected abstract val files: FileOperations

    @TaskAction
    fun build() {
        val buildsForWindows = toolchain.get() == CargoToolchain.x86Windows

        installCargoCrossCompiler(buildsForWindows)
        buildForTarget(buildsForWindows)

        val profile = if (release.get()) "release" else "debug"
        val cargoOutputDir =
            File(cargoToml.get().asFile.parent, "target/${toolchain.get().target}/${profile}")

        files.copy {
            from(cargoOutputDir)
            into(destinationDirectory.dir(toolchain.get().folder))

            include("lib${libname.get()}.so")
            include("lib${libname.get()}.dylib")
            include("${libname.get()}.dll")
        }
    }

    private fun installCargoCrossCompiler(buildsForWindows: Boolean) {
        exec.exec {
            // Pin version to latest 0.6.6 RC to due to issue
            // https://github.com/Jake-Shadle/xwin/issues/141
            // Version should be removed, once 0.6.6 or newer is officially available
            val crossCompiler = if (buildsForWindows) "xwin@0.6.6-rc.2" else "cargo-zigbuild"
            commandLine = listOf(cargoBin.get() + "/cargo", "install", "--locked", crossCompiler)
        }
        if (buildsForWindows && !File(xwinFolder.get()).exists()) {
            exec.exec {
                // https://github.com/Jake-Shadle/xwin/issues/141#issuecomment-2416864318
                environment("RAYON_NUM_THREADS", "1")
                commandLine =
                    listOf(
                        cargoBin.get() + "/xwin",
                        "--accept-license",
                        "splat",
                        "--output",
                        xwinFolder.get()
                    )
            }
        }
    }

    private fun buildForTarget(buildsForWindows: Boolean) {
        exec.exec {
            val buildCommand = if (buildsForWindows) "build" else "zigbuild"

            workingDir = cargoToml.get().asFile.parentFile

            commandLine =
                listOf(
                    cargoBin.get() + "/cargo",
                    buildCommand,
                    "--target=${toolchain.get().target}"
                )

            if (buildsForWindows) {
                // See https://github.com/Jake-Shadle/xwin/blob/main/xwin.dockerfile
                val xwin = xwinFolder.get()
                val clFlags =
                    "-Wno-unused-command-line-argument -fuse-ld=lld-link /vctoolsdir $xwin/crt /winsdkdir $xwin/sdk"
                environment("CC_x86_64_pc_windows_msvc", "clang-cl")
                environment("CXX_x86_64_pc_windows_msvc", "clang-cl")
                environment("AR_x86_64_pc_windows_msvc", "llvm-lib")
                environment("WINEDEBUG", "-all")
                environment("CARGO_TARGET_X86_64_PC_WINDOWS_MSVC_RUNNER", "wine")
                environment("CL_FLAGS", clFlags)
                environment("CFLAGS_x86_64_pc_windows_msvc", clFlags)
                environment("CXXFLAGS_x86_64_pc_windows_msvc", clFlags)
                environment("CARGO_TARGET_X86_64_PC_WINDOWS_MSVC_LINKER", "lld-link")
                environment(
                    "RUSTFLAGS",
                    "-Lnative=$xwin/crt/lib/x86_64 -Lnative=$xwin/sdk/lib/um/x86_64 -Lnative=$xwin/sdk/lib/ucrt/x86_64"
                )
            }

            if (release.get()) {
                args("--release")
            }
            if (logger.isEnabled(LogLevel.INFO)) {
                // For '--info' logging, turn on '--verbose'
                args("--verbose")
            }
        }
    }
}
