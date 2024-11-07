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

plugins { id("java") }

tasks.register<WriteProperties>("writeGitProperties") {
    property("git.build.version", project.version)
    @Suppress("UnstableApiUsage")
    property(
        "git.commit.id",
        providers
            .exec { commandLine("git", "rev-parse", "HEAD") }
            .standardOutput
            .asText
            .map { it.trim() }
    )
    @Suppress("UnstableApiUsage")
    property(
        "git.commit.id.abbrev",
        providers
            .exec { commandLine("git", "rev-parse", "HEAD") }
            .standardOutput
            .asText
            .map { it.trim().substring(0, 7) }
    )

    destinationFile = layout.buildDirectory.file("generated/git/git.properties")
}

tasks.processResources { from(tasks.named("writeGitProperties")) }

// ignore the content of 'git.properties' when using a classpath as task input
normalization.runtimeClasspath { ignore("git.properties") }
