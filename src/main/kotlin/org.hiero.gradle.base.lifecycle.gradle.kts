// SPDX-License-Identifier: Apache-2.0
plugins { id("base") }

// Convenience for local development: when running './gradlew' without parameters, show the tasks...
defaultTasks("tasks")

if (gradle.startParameter.taskNames.isEmpty()) {
    // ...of group 'build' only
    tasks.withType<TaskReportTask>().configureEach { displayGroup = "build" }
}

tasks.register("qualityCheck") {
    group = "verification"
    description = "Run all spotless and quality checks."
    dependsOn(tasks.assemble)
}

tasks.register("qualityGate") {
    group = "build"
    description = "Apply spotless rules and run all quality checks."
    dependsOn(tasks.assemble)
}

tasks.check { dependsOn(tasks.named("qualityCheck")) }

afterEvaluate {
    tasks.configureEach {
        if (name in listOf("buildDependents", "buildNeeded", "classes")) {
            group = null
        }
        if (name.endsWith("Classes")) {
            group = null
        }
        if (this is Jar) {
            group = null
        }
        if (this is Test) {
            group = "build"
        }
        // added by Kotlin if used
        if (name in listOf("buildKotlinToolingMetadata", "kotlinSourcesJar")) {
            group = null
        }
        // added by Spring Boot plugin if used
        if (name in listOf("resolveMainClassName", "resolveTestMainClassName")) {
            group = null
        }
    }
}
