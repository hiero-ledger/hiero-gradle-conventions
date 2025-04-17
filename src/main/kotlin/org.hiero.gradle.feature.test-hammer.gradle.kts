// SPDX-License-Identifier: Apache-2.0
import org.hiero.gradle.services.TaskLockService

plugins { id("java") }

// Test functionally correct behavior under stress/loads with many repeated iterations.
@Suppress("UnstableApiUsage")
testing.suites {
    register<JvmTestSuite>("hammer") {
        targets.all {
            testTask {
                maxHeapSize = "8g"
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
    getByName("hammerCompileOnly") { extendsFrom(configurations.compileOnly.get()) }
    getByName("hammerImplementation") { extendsFrom(configurations.implementation.get()) }
    getByName("hammerRuntimeOnly") { extendsFrom(configurations.runtimeOnly.get()) }
}
