// SPDX-License-Identifier: Apache-2.0
import org.hiero.gradle.services.TaskLockService

plugins {
    id("java")
    // id("org.gradlex.java-module-testing")
}

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

configurations.testCompileOnly { extendsFrom(configurations.compileOnly.get()) }

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
