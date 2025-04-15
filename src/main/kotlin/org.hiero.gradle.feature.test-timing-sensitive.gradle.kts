// SPDX-License-Identifier: Apache-2.0
import org.hiero.gradle.services.TaskLockService

plugins { id("java") }

// Tests that are resource sensitive (e.g. use Thread.sleep()) and thus need to run without anything
// in parallel.
@Suppress("UnstableApiUsage")
testing.suites {
    register<JvmTestSuite>("timingSensitive") {
        targets.all {
            testTask {
                maxHeapSize = "4g"
                usesService(
                    gradle.sharedServices.registerIfAbsent("lock", TaskLockService::class) {
                        maxParallelUsages = 1
                    }
                )
            }
        }
    }
}

// Link 'main' dependency scopes similar as they are linked for 'test'
// https://docs.gradle.org/current/userguide/java_library_plugin.html#sec:java_library_configurations_graph
configurations {
    getByName("timingSensitiveCompileOnly") { extendsFrom(configurations.compileOnly.get()) }
    getByName("timingSensitiveImplementation") { extendsFrom(configurations.implementation.get()) }
    getByName("timingSensitiveRuntimeOnly") { extendsFrom(configurations.runtimeOnly.get()) }
}
