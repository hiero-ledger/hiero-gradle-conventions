// SPDX-License-Identifier: Apache-2.0
package org.hiero.gradle.extensions

import javax.inject.Inject
import org.gradle.api.Project
import org.gradle.api.file.ProjectLayout
import org.gradle.api.provider.Property
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.TaskContainer
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import org.gradle.nativeplatform.MachineArchitecture.ARM64
import org.gradle.nativeplatform.MachineArchitecture.X86
import org.gradle.nativeplatform.MachineArchitecture.X86_64
import org.gradle.nativeplatform.OperatingSystemFamily.LINUX
import org.gradle.nativeplatform.OperatingSystemFamily.MACOS
import org.gradle.nativeplatform.OperatingSystemFamily.WINDOWS
import org.hiero.gradle.environment.EnvAccess
import org.hiero.gradle.tasks.CargoBuildTask
import org.hiero.gradle.tasks.CargoVersions

@Suppress("LeakingThis")
abstract class CargoExtension {
    abstract val libname: Property<String>
    abstract val appname: Property<String>
    abstract val javaPackage: Property<String>
    abstract val release: Property<Boolean>

    @get:Inject protected abstract val project: Project

    @get:Inject protected abstract val layout: ProjectLayout

    @get:Inject protected abstract val tasks: TaskContainer

    @get:Inject protected abstract val providers: ProviderFactory

    @get:Inject protected abstract val sourceSets: SourceSetContainer

    init {
        @Suppress("UnstableApiUsage") val rootDir = project.isolated.rootProject.projectDirectory
        val versions = EnvAccess.toolchainVersions(rootDir, providers, project.objects)

        tasks.withType<CargoVersions>().configureEach {
            rustVersion.convention(versions.getting("rust"))
            cargoZigbuildVersion.convention(versions.getting("cargo-zigbuild"))
            zigVersion.convention(versions.getting("zig"))
            xwinVersion.convention(versions.getting("xwin"))
        }

        // By default, assume we only build a library:
        libname.convention(project.name)
        appname.convention("")

        javaPackage.convention("com.hedera.nativelib")
        release.convention(true)
    }

    fun targets(vararg targets: CargoToolchain) {
        val installTask = ":installRustToolchains" // in root project
        @Suppress("UnstableApiUsage")
        val installDir = project.isolated.rootProject.projectDirectory.dir("build/rust-toolchains")

        val packageAllTargets =
            providers.gradleProperty("packageAllTargets").getOrElse("false").toBoolean()
        val skipInstall =
            providers.gradleProperty("skipInstallRustToolchains").getOrElse("false").toBoolean()

        targets.forEach { target ->
            val targetBuildTask =
                tasks.register<CargoBuildTask>(
                    "cargoBuild${target.name.replaceFirstChar(Char::titlecase)}"
                ) {
                    group = "rust"
                    description = "Build library ($target)"

                    if (!skipInstall) {
                        dependsOn(installTask)
                    }

                    toolchain.convention(target)
                    sourcesDirectory.convention(layout.projectDirectory.dir("src/main/rust"))
                    destinationDirectory.convention(
                        layout.buildDirectory.dir("rustJniLibs/${target.platform}")
                    )

                    cargoToml.convention(layout.projectDirectory.file("Cargo.toml"))
                    libname.convention(this@CargoExtension.libname)
                    appname.convention(this@CargoExtension.appname)
                    javaPackage.convention(this@CargoExtension.javaPackage)
                    release.convention(this@CargoExtension.release)
                    rustInstallFolder.convention(installDir)
                }

            if (packageAllTargets || isHostTarget(target)) {
                sourceSets.getByName("main").resources.srcDir(targetBuildTask)
            }
        }
    }

    // inspired by 'org.gradlex.javamodule.packaging.internal.HostIdentification'
    fun isHostTarget(target: CargoToolchain): Boolean {
        return target.os == hostOs() && target.arch == hostArch()
    }

    companion object {
        fun hostOs(): String {
            val os = System.getProperty("os.name").lowercase().replace(" ", "")
            if (os.contains("windows")) {
                return WINDOWS
            }
            if (os.contains("macos") || os.contains("darwin") || os.contains("osx")) {
                return MACOS
            }
            return LINUX
        }

        fun hostArch(): String {
            val arch = System.getProperty("os.arch").lowercase()
            if (arch.contains("aarch")) {
                @Suppress("UnstableApiUsage")
                return ARM64
            }
            if (arch.contains("64")) {
                return X86_64
            }
            return X86
        }
    }
}
