// SPDX-License-Identifier: Apache-2.0
package org.hiero.gradle.tasks

import javax.inject.Inject
import org.gradle.StartParameter
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.ProjectLayout
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import org.gradle.work.DisableCachingByDefault

@Suppress("LeakingThis")
@DisableCachingByDefault(because = "processes large amount of data")
abstract class GitClone : DefaultTask() {

    @get:Input abstract val url: Property<String>

    @get:Input @get:Optional abstract val tag: Property<String>

    @get:Input @get:Optional abstract val branch: Property<String>

    @get:Input abstract val offline: Property<Boolean>

    /** Patch files applied, in order, with 'git apply' after the checkout. */
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val patches: ConfigurableFileCollection

    @get:OutputDirectory abstract val localCloneDirectory: DirectoryProperty

    @get:Inject protected abstract val exec: ExecOperations

    @get:Inject protected abstract val startParameter: StartParameter

    @get:Inject protected abstract val layout: ProjectLayout

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
                    commandLine("git", "clone", url.get(), localClone.asFile.absolutePath, "-q")
                } else {
                    workingDir = localClone.asFile
                    commandLine("git", "fetch", "-q")
                }
            }
        }
        // '-f' discards previously applied patches, which may conflict with the checkout
        if (tag.isPresent) {
            exec.exec {
                workingDir = localClone.asFile
                commandLine("git", "checkout", "-f", tag.get(), "-q")
            }
            exec.exec {
                workingDir = localClone.asFile
                commandLine("git", "reset", "--hard", tag.get(), "-q")
            }
        } else {
            exec.exec {
                workingDir = localClone.asFile
                commandLine("git", "checkout", "-f", branch.get(), "-q")
            }
            exec.exec {
                workingDir = localClone.asFile
                commandLine("git", "reset", "--hard", "origin/${branch.get()}", "-q")
            }
        }
        // '--index' stages files added by a patch, so that the next 'reset --hard' removes them
        patches.forEach { patch ->
            exec.exec {
                workingDir = localClone.asFile
                commandLine("git", "apply", "--index", patch.absolutePath)
            }
        }
    }
}
