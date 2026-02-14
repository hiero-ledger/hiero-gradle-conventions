// SPDX-License-Identifier: Apache-2.0
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
abstract class CargoBuildTask : CargoVersions, DefaultTask() {

    @get:Input abstract val libname: Property<String>
    @get:Input abstract val appname: Property<String>

    @get:Input abstract val javaPackage: Property<String>

    @get:Input abstract val release: Property<Boolean>

    @get:Input abstract val toolchain: Property<CargoToolchain>

    @get:Internal // the toolchain versions are tracked as input
    abstract val rustInstallFolder: DirectoryProperty

    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val cargoToml: RegularFileProperty

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.NAME_ONLY)
    abstract val sourcesDirectory: DirectoryProperty

    @get:OutputDirectory abstract val destinationDirectory: DirectoryProperty

    @get:Inject protected abstract val exec: ExecOperations

    @get:Inject protected abstract val files: FileOperations

    @TaskAction
    fun build() {
        val buildsForWindows = toolchain.get() == CargoToolchain.x86Windows

        buildForTarget(buildsForWindows)

        val profile = if (release.get()) "release" else "debug"
        val cargoOutputDir =
            File(
                cargoToml.get().asFile.parent,
                "target/${toolchain.get().targetWithoutVersion()}/${profile}",
            )

        if (!libname.get().equals("")) {
            files.sync {
                val baseFolder = javaPackage.get().replace('.', '/')
                val targetFolder = baseFolder + "/" + libname.get() + "/" + toolchain.get().folder

                from(cargoOutputDir)
                into(destinationDirectory.dir(targetFolder))

                include("lib${libname.get()}.so")
                include("lib${libname.get()}.dylib")
                include("${libname.get()}.dll")
            }
        }

        if (!appname.get().equals("")) {
            files.sync {
                val baseFolder = javaPackage.get().replace('.', '/')
                val targetFolder = baseFolder + "/" + appname.get() + "/" + toolchain.get().folder

                from(cargoOutputDir)
                into(destinationDirectory.dir(targetFolder))

                include("${appname.get()}")
                include("${appname.get()}.exe")
            }
        }
    }

    private fun buildForTarget(buildsForWindows: Boolean) {
        val rustupHome = rustInstallFolder.dir("rustup").get().asFile.absolutePath
        val cargoHome = rustInstallFolder.dir("cargo").get().asFile
        val zigPath = rustInstallFolder.file("zig/zig").get().asFile.absolutePath
        val buildCommand = if (buildsForWindows) "build" else "zigbuild"

        exec.exec {
            workingDir = cargoToml.get().asFile.parentFile

            environment("RUSTUP_HOME", rustupHome)
            environment("CARGO_HOME", cargoHome)
            environment("CARGO_ZIGBUILD_ZIG_PATH", zigPath)

            commandLine =
                listOf(
                    File(cargoHome, "bin/cargo").absolutePath,
                    "+${rustVersion.get()}",
                    buildCommand,
                    "--target=${toolchain.get().target}",
                )

            if (buildsForWindows) {
                // See https://github.com/Jake-Shadle/xwin/blob/main/xwin.dockerfile
                val xwinFolder = rustInstallFolder.dir("xwin").get().asFile.absolutePath
                val rustupToolchains = rustInstallFolder.dir("rustup/toolchains").get().asFile
                val rustLld = rustupToolchains.walk().filter { it.name == "rust-lld" }.single()

                val clFlags =
                    "-Wno-unused-command-line-argument -fuse-ld=lld-link /vctoolsdir $xwinFolder/crt /winsdkdir $xwinFolder/sdk"
                environment("CC_x86_64_pc_windows_msvc", "clang-cl")
                environment("CXX_x86_64_pc_windows_msvc", "clang-cl")
                environment("AR_x86_64_pc_windows_msvc", "llvm-lib")
                environment("WINEDEBUG", "-all")
                environment("CARGO_TARGET_X86_64_PC_WINDOWS_MSVC_RUNNER", "wine")
                environment("CL_FLAGS", clFlags)
                environment("CFLAGS_x86_64_pc_windows_msvc", clFlags)
                environment("CXXFLAGS_x86_64_pc_windows_msvc", clFlags)
                environment("CARGO_TARGET_X86_64_PC_WINDOWS_MSVC_LINKER", rustLld.absolutePath)
                environment(
                    "RUSTFLAGS",
                    "-Lnative=$xwinFolder/crt/lib/x86_64 -Lnative=$xwinFolder/sdk/lib/um/x86_64 -Lnative=$xwinFolder/sdk/lib/ucrt/x86_64",
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
