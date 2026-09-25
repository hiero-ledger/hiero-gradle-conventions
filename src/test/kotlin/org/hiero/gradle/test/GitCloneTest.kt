// SPDX-License-Identifier: Apache-2.0
package org.hiero.gradle.test

import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.TaskOutcome
import org.hiero.gradle.test.fixtures.GradleProject
import org.junit.jupiter.api.Test

class GitCloneTest {

    private val p = GradleProject().withMinimalStructure()
    private val upstream = p.dir("upstream")
    private val clone = p.dir("product/module-a/build/clone")

    init {
        upstream.mkdirs()
        git("init", "-q", "-b", "main")
        upstream.resolve("README.txt").writeText("v1\n")
        git("add", "-A")
        git("commit", "-q", "-m", "v1")
        git("tag", "v1.0.0")
        upstream.resolve("README.txt").writeText("v2\n")
        git("commit", "-q", "-a", "-m", "v2")
        git("tag", "v2.0.0")

        git("checkout", "-q", "-b", "patches", "v1.0.0")
        upstream.resolve("README.txt").writeText("v1 patched\n")
        upstream.resolve("ADDED.txt").writeText("added\n")
        git("add", "-A")
        git("commit", "-q", "-m", "patch 1")
        upstream.resolve("ADDED.txt").writeText("added and patched again\n")
        git("commit", "-q", "-a", "-m", "patch 2")
        p.file("product/module-a/patches/01.patch", git("diff", "v1.0.0", "HEAD~1"))
        p.file("product/module-a/patches/02.patch", git("diff", "HEAD~1", "HEAD"))
        git("checkout", "-q", "main")
    }

    @Test
    fun `clones a tag and applies patches in order`() {
        cloneTask(tag = "v1.0.0", patches = listOf("patches/01.patch", "patches/02.patch"))

        val result = p.run(":module-a:clone")
        val rerun = p.run(":module-a:clone")

        assertThat(result.task(":module-a:clone")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
        assertThat(rerun.task(":module-a:clone")?.outcome).isEqualTo(TaskOutcome.UP_TO_DATE)
        assertThat(clone.resolve("README.txt")).hasContent("v1 patched")
        assertThat(clone.resolve("ADDED.txt")).hasContent("added and patched again")
    }

    @Test
    fun `changes of previously applied patches are discarded`() {
        cloneTask(tag = "v1.0.0", patches = listOf("patches/01.patch"))
        p.run(":module-a:clone")
        cloneTask(tag = "v2.0.0", patches = emptyList())

        val result = p.run(":module-a:clone")

        assertThat(result.task(":module-a:clone")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
        assertThat(clone.resolve("README.txt")).hasContent("v2")
        assertThat(clone.resolve("ADDED.txt")).doesNotExist()
    }

    @Test
    fun `fails if a patch does not apply`() {
        cloneTask(tag = "v2.0.0", patches = listOf("patches/01.patch"))

        val result = p.runAndFail(":module-a:clone")

        assertThat(result.task(":module-a:clone")?.outcome).isEqualTo(TaskOutcome.FAILED)
        assertThat(result.output).contains("README.txt: patch does not apply")
    }

    private fun cloneTask(tag: String, patches: List<String>) {
        p.moduleBuildFile(
            """
            tasks.register<org.hiero.gradle.tasks.GitClone>("clone") {
                url = "${upstream.absoluteFile.invariantSeparatorsPath}"
                tag = "$tag"
                patches.from(${patches.joinToString { "\"$it\"" }})
                localCloneDirectory = layout.buildDirectory.dir("clone")
            }
            """
                .trimIndent()
        )
    }

    private fun git(vararg args: String): String {
        val process =
            ProcessBuilder(
                    listOf(
                        "git",
                        "-c",
                        "user.name=test",
                        "-c",
                        "user.email=test@example.com",
                        "-c",
                        "commit.gpgsign=false",
                    ) + args
                )
                .directory(upstream)
                .redirectErrorStream(true)
                .start()
        val output = process.inputStream.bufferedReader().readText()
        check(process.waitFor() == 0) { "git ${args.joinToString(" ")} failed: $output" }
        return output
    }
}
