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
import org.hiero.gradle.environment.EnvAccess
import org.hiero.gradle.tasks.CargoBuildTask
import org.hiero.gradle.tasks.CargoVersions

@Suppress("LeakingThis")
abstract class CargoExtension {
    abstract val libname: Property<String>
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

        libname.convention(project.name)
        javaPackage.convention("com.hedera.nativelib")
        release.convention(true)

        // Lifecycle task to only do all carg build tasks (mainly for testing)
        project.tasks.register("cargoBuild") {
            group = "rust"
            description = "Build library (all targets)"
        }
    }

    fun targets(vararg targets: CargoToolchain) {
        val installTask = ":installRustToolchains" // in root project
        @Suppress("UnstableApiUsage")
        val installDir = project.isolated.rootProject.projectDirectory.dir("build/rust-toolchains")
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
                    javaPackage.convention(this@CargoExtension.javaPackage)
                    release.convention(this@CargoExtension.release)
                    rustInstallFolder.convention(installDir)
                }

            tasks.named("cargoBuild") { dependsOn(targetBuildTask) }
            sourceSets.getByName("main").resources.srcDir(targetBuildTask)
        }
    }
}
