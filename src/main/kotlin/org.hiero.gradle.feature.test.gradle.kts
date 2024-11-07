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

import org.hiero.gradle.services.TaskLockService

plugins { id("java") }

@Suppress("UnstableApiUsage")
testing.suites {
    named<JvmTestSuite>("test") {
        useJUnitJupiter()
        targets.all {
            testTask {
                maxHeapSize = "4g"
                // Some tests overlap due to using the same temp folders within one project
                // maxParallelForks = 4 <- set this, once tests can run in parallel

                // Enable dynamic agent loading for tests - eg: Mockito, ByteBuddy
                jvmArgs("-XX:+EnableDynamicAgentLoading")
            }
        }
    }
    // remove automatically added compile time dependencies, as we define them explicitly
    withType<JvmTestSuite> {
        configurations.getByName(sources.implementationConfigurationName) {
            withDependencies {
                removeIf { it.group == "org.junit.jupiter" && it.name == "junit-jupiter" }
            }
        }
        dependencies { runtimeOnly("org.junit.jupiter:junit-jupiter-engine") }
    }
}

// If user gave the argument '-PactiveProcessorCount', then do:
// - run all test tasks in sequence
// - give the -XX:ActiveProcessorCount argument to the test JVMs
val activeProcessorCount = providers.gradleProperty("activeProcessorCount")

if (activeProcessorCount.isPresent) {
    tasks.withType<Test>().configureEach {
        usesService(
            gradle.sharedServices.registerIfAbsent("lock", TaskLockService::class) {
                maxParallelUsages = 1
            }
        )
        jvmArgs("-XX:ActiveProcessorCount=${activeProcessorCount.get()}")
    }
}
