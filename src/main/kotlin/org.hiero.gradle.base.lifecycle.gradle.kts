/*
 * Copyright (C) 2016-2024 Hiero a Series of LF Projects, LLC
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

plugins { id("base") }

// Convenience for local development: when running './gradlew' without any parameters show the tasks
// from the 'build' group
defaultTasks("tasks")

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

tasks.register("releaseMavenCentral") { group = "release" }

afterEvaluate {
    tasks.configureEach {
        if (name in listOf("buildDependents", "buildNeeded", "classes")) {
            group = null
        }
        if (name.endsWith("Classes")) {
            group = null
        }
        if (this is Jar) {
            setGroup(null)
        }
        if (this is Test) {
            group = "build"
        }
    }
}
