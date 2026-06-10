// SPDX-License-Identifier: Apache-2.0
package org.hiero.gradle.problems

import javax.inject.Inject
import org.gradle.StartParameter
import org.gradle.api.logging.configuration.WarningMode
import org.gradle.api.problems.ProblemGroup
import org.gradle.api.problems.ProblemId
import org.gradle.api.problems.Problems

/** Helper to use the Gradle Problems API during build configuration. */
@Suppress("UnstableApiUsage")
abstract class ProblemReporter
@Inject
constructor(val problems: Problems, val startParameters: StartParameter) {

    /**
     * Add a warning to the 'Problems report' (a link to the report shows at the end of the build).
     */
    fun warn(displayName: String, description: String, file: String, solution: String) {
        val fail = startParameters.warningMode == WarningMode.Fail
        val group = ProblemGroup.create("configuration", "Build Configuration")
        val problemId =
            ProblemId.create(displayName.lowercase().replace(" ", "-"), displayName, group)

        val problem =
            problems.reporter.create(problemId) {
                if (!description.isEmpty()) {
                    details(description)
                }
                solution(solution)
                fileLocation(file)
                documentedAt(
                    "https://github.com/hiero-ledger/hiero-gradle-conventions#project-structure"
                )
            }

        if (fail) {
            problems.reporter.throwing(IllegalStateException(displayName), problem)
        } else {
            problems.reporter.report(problem)
        }
    }
}
