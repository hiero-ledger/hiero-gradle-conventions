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

import javax.inject.Inject
import org.gradle.api.Project
import org.gradle.api.file.ProjectLayout
import org.gradle.api.provider.Property
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.TaskContainer
import org.gradle.kotlin.dsl.register
import org.hiero.gradle.tasks.CargoBuildTask

@Suppress("LeakingThis")
abstract class CargoExtension {
    abstract val cargoBin: Property<String>
    abstract val libname: Property<String>
    abstract val release: Property<Boolean>

    @get:Inject protected abstract val project: Project

    @get:Inject protected abstract val layout: ProjectLayout

    @get:Inject protected abstract val tasks: TaskContainer

    @get:Inject protected abstract val sourceSets: SourceSetContainer

    init {
        cargoBin.convention(System.getProperty("user.home") + "/.cargo/bin")
        libname.convention(project.name)
        release.convention(true)

        // Lifecycle task to only do all carg build tasks (mainly for testing)
        project.tasks.register("cargoBuild") {
            group = "rust"
            description = "Build library (all targets)"
        }
    }

    fun targets(vararg targets: CargoToolchain) {
        targets.forEach { target ->
            val targetBuildTask =
                tasks.register<CargoBuildTask>(
                    "cargoBuild${target.name.replaceFirstChar(Char::titlecase)}"
                ) {
                    group = "rust"
                    description = "Build library ($target)"
                    toolchain.convention(target)
                    sourcesDirectory.convention(layout.projectDirectory.dir("src/main/rust"))
                    destinationDirectory.convention(
                        layout.buildDirectory.dir("rustJniLibs/${target.platform}")
                    )

                    this.cargoToml.convention(layout.projectDirectory.file("Cargo.toml"))
                    this.libname.convention(this@CargoExtension.libname)
                    this.release.convention(this@CargoExtension.release)
                    this.cargoBin.convention(this@CargoExtension.cargoBin)
                    @Suppress("UnstableApiUsage")
                    this.xwinFolder.convention(
                        project.isolated.rootProject.projectDirectory
                            .dir(".gradle/xwin")
                            .asFile
                            .absolutePath
                    )
                }

            tasks.named("cargoBuild") { dependsOn(targetBuildTask) }
            sourceSets.getByName("main").resources.srcDir(targetBuildTask)
        }
    }
}
