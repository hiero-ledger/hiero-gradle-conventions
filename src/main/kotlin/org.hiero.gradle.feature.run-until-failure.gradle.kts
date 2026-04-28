// SPDX-License-Identifier: Apache-2.0
import org.hiero.gradle.extensions.RunUntilFailureExtension
import org.hiero.gradle.tasks.RunUntilFailureTask

val runUntilFailure = extensions.create<RunUntilFailureExtension>("runUntilFailure")

tasks.register<RunUntilFailureTask>("runUntilFailure") {
    group = "verification"
    description = "Runs a test task repeatedly until it fails."

    testTaskName.set(runUntilFailure.testTaskName)
    maxRetries.set(runUntilFailure.maxRetries)
    testFilters.set(runUntilFailure.testFilters)
    projectDir.set(rootProject.projectDir)
    hostProjectPath.set(project.path)
}
