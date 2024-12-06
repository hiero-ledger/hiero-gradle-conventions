// SPDX-License-Identifier: Apache-2.0
package org.hiero.gradle.extensions

import java.util.Properties
import javax.inject.Inject
import org.gradle.api.Project
import org.gradle.api.file.ProjectLayout
import org.gradle.api.provider.Property
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.TaskContainer
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import org.hiero.gradle.tasks.CargoBuildTask
import org.hiero.gradle.tasks.CargoVersions
import org.hiero.gradle.tasks.RustToolchainInstallTask

@Suppress("LeakingThis")
abstract class CargoExtension {
    abstract val libname: Property<String>
    abstract val release: Property<Boolean>

    @get:Inject protected abstract val project: Project

    @get:Inject protected abstract val layout: ProjectLayout

    @get:Inject protected abstract val tasks: TaskContainer

    @get:Inject protected abstract val providers: ProviderFactory

    @get:Inject protected abstract val sourceSets: SourceSetContainer

    init {
        @Suppress("UnstableApiUsage")
        val versionsFile =
            project.isolated.rootProject.projectDirectory.file(
                "gradle/toolchain-versions.properties"
            )
        val versions = Properties()
        versions.load(
            providers
                .fileContents(versionsFile)
                .asText
                .orElse(
                    providers.provider {
                        throw RuntimeException("${versionsFile.asFile} does not exist")
                    }
                )
                .get()
                .reader()
        )

        tasks.withType<CargoVersions>().configureEach {
            rustVersion.convention(versions.getValue("rust") as String)
            cargoZigbuildVersion.convention(versions.getValue("cargo-zigbuild") as String)
            zigVersion.convention(versions.getValue("zig") as String)
            xwinVersion.convention(versions.getValue("xwin") as String)
        }

        libname.convention(project.name)
        release.convention(true)

        // Rust toolchain installation
        tasks.register<RustToolchainInstallTask>("installRustToolchains") {
            group = "rust"
            description = "Installs Rust and toolchain components required for cross-compilation"

            // Track host system as input as the task output differs between operating systems
            hostOperatingSystem.set(readHostOperatingSystem())
            hostArchitecture.set(System.getProperty("os.arch"))

            toolchains.convention(CargoToolchain.values().asList())
            destinationDirectory.convention(layout.buildDirectory.dir("rust-toolchains"))
        }

        // Lifecycle task to only do all carg build tasks (mainly for testing)
        project.tasks.register("cargoBuild") {
            group = "rust"
            description = "Build library (all targets)"
        }
    }

    private fun readHostOperatingSystem() =
        System.getProperty("os.name").lowercase().let {
            if (it.contains("windows")) {
                "windows"
            } else if (it.contains("mac")) {
                "macos"
            } else {
                "linux"
            }
        }

    fun targets(vararg targets: CargoToolchain) {
        val installTask = tasks.named<RustToolchainInstallTask>("installRustToolchains")
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
                    release.convention(this@CargoExtension.release)
                    rustInstallFolder.convention(installTask.flatMap { it.destinationDirectory })
                }

            tasks.named("cargoBuild") { dependsOn(targetBuildTask) }
            sourceSets.getByName("main").resources.srcDir(targetBuildTask)
        }
    }
}
