/*
 * Copyright (C) 2022-2024 Hiero a Series of LF Projects, LLC
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

plugins {
    id("org.hiero.gradle.base.lifecycle")
    id("com.diffplug.spotless")
}

spotless {
    // Disable the automatic application of Spotless to all source sets when the check task is run.
    isEnforceCheck = false

    // limit format enforcement to just the files changed by this feature branch
    @Suppress("UnstableApiUsage")
    ratchetFrom(
        "origin/" +
            providers
                .fileContents(
                    isolated.rootProject.projectDirectory.file("gradle/development-branch.txt")
                )
                .asText
                .getOrElse("main")
    )
}

tasks.withType<JavaCompile>().configureEach {
    // When doing a 'qualityGate' run, make sure spotlessApply is done before doing compilation and
    // other checks based on compiled code
    mustRunAfter(tasks.spotlessApply)
}

tasks.named("qualityCheck") { dependsOn(tasks.spotlessCheck) }

tasks.named("qualityGate") { dependsOn(tasks.spotlessApply) }
