// SPDX-License-Identifier: Apache-2.0
package org.hiero.gradle.problems

import javax.inject.Inject
import org.gradle.api.problems.ProblemGroup
import org.gradle.api.problems.ProblemId
import org.gradle.api.problems.Problems

/** Helper to use the Gradle Problems API during build configuration. */
@Suppress("UnstableApiUsage")
abstract class ProblemReporter @Inject constructor(val problems: Problems) {

    /**
     * Add a warning to the 'Problems report' (a link to the report shows at the end of the build).
     */
    fun warn(displayName: String, description: String, file: String, solution: String) {
        val group = ProblemGroup.create("configuration", "Build Configuration")
        val problemId =
            ProblemId.create(displayName.lowercase().replace(" ", "-"), displayName, group)
        problems.reporter.report(problemId) {
            if (!description.isEmpty()) {
                this.details(description)
            }
            solution(solution)
            fileLocation(file)
            documentedAt(
                "https://github.com/hiero-ledger/hiero-gradle-conventions#project-structure"
            )
        }
    }
}
