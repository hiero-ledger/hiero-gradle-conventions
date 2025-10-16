// SPDX-License-Identifier: Apache-2.0
package org.hiero.gradle.tasks

import javax.inject.Inject
import org.gradle.StartParameter
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.ProjectLayout
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations

@Suppress("LeakingThis")
abstract class GitClone : DefaultTask() {

    @get:Input abstract val url: Property<String>

    @get:Input @get:Optional abstract val tag: Property<String>

    @get:Input @get:Optional abstract val branch: Property<String>

    @get:Input abstract val offline: Property<Boolean>

    @get:OutputDirectory abstract val localCloneDirectory: DirectoryProperty

    @get:Inject protected abstract val exec: ExecOperations

    @get:Inject protected abstract val startParameter: StartParameter

    @get:Inject protected abstract val layout: ProjectLayout

    @get:Input @get:Optional abstract val noCheckout: Property<Boolean>

    @get:Input @get:Optional abstract val sparseClone: Property<Boolean>

    @get:Input @get:Optional abstract val cloneDepth: Property<Int>

    @get:Input @get:Optional abstract val filter: Property<String>

    @get:Input @get:Optional abstract val sparseCheckoutPaths: Property<List<String>>

    @get:Input @get:Optional abstract val reset: Property<Boolean>


    init {
        offline.set(startParameter.isOffline)
        // If a 'branch' is configured, the task is never up-to-date as it may change
        outputs.upToDateWhen { !branch.isPresent }
    }

    @TaskAction
    fun cloneOrUpdate() {
        if (!tag.isPresent && !branch.isPresent || tag.isPresent && branch.isPresent) {
            throw RuntimeException("Define either 'tag' or 'branch'")
        }

        val localClone = localCloneDirectory.get()
        if (!offline.get()) {
            exec.exec {
                if (!localClone.dir(".git").asFile.exists()) {
                    workingDir = localClone.asFile.parentFile
                    // build clone command
                    val gitCloneStringList: ArrayList<String> = arrayListOf("git", "clone")

                    if (sparseClone.isPresent && sparseClone.get()) {
                        gitCloneStringList.add("--sparse")
                    }

                    if (cloneDepth.isPresent) {
                        gitCloneStringList.add("--depth=${cloneDepth.get()}")
                    }

                    if (noCheckout.isPresent && noCheckout.get()) {
                        gitCloneStringList.add("--no-checkout")
                    }

                    if (filter.isPresent) {
                        gitCloneStringList.add("--filter=${filter.get()}")
                    }

                    // add final url and quiet params
                    gitCloneStringList.add(url.get())
                    gitCloneStringList.add("-q")
                    commandLine(gitCloneStringList)
                } else {
                    workingDir = localClone.asFile
                    commandLine("git", "fetch", "-q")
                }
            }
        }

        // handle sparse checkout
        if (sparseCheckoutPaths.isPresent) {
            exec.exec {
                workingDir = localClone.asFile
                commandLine("git", "sparse-checkout", "init")
            }

            // build spare-checkout set command
            val gitSparseCheckoutSetStringList: ArrayList<String> =
                arrayListOf("git", "sparse-checkout", "set")
            sparseCheckoutPaths.get().forEach { path -> gitSparseCheckoutSetStringList.add(path) }

            gitSparseCheckoutSetStringList.add("-q")
            exec.exec {
                workingDir = localClone.asFile
                commandLine(gitSparseCheckoutSetStringList)
            }
        }

        if (!reset.isPresent) {
            // build checkout command
            val gitCheckoutStringList: ArrayList<String> = arrayListOf("git", "checkout")

            if (tag.isPresent) {
                gitCheckoutStringList.add(tag.get())
            } else {
                gitCheckoutStringList.add("origin/${branch.get()}")
            }

            gitCheckoutStringList.add("-q")
            exec.exec {
                workingDir = localClone.asFile
                commandLine(gitCheckoutStringList)
            }
        }
        if (reset.isPresent && reset.get()) {
            // build reset command
            val gitResetStringList: ArrayList<String> = arrayListOf("git", "reset", "--hard")

            if (tag.isPresent) {
                gitResetStringList.add(tag.get())
            } else {
                gitResetStringList.add("origin/${branch.get()}")
            }

            gitResetStringList.add("-q")
            exec.exec {
                workingDir = localClone.asFile
                commandLine(gitResetStringList)
            }
        }

        println("Successfully cloned or updated $url to ${localClone.asFile.absolutePath}")
    }
}
