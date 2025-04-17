// SPDX-License-Identifier: Apache-2.0
plugins { id("java") }

// Tests that could be in the default "test" set but take more than 100ms to execute.
@Suppress("UnstableApiUsage")
testing.suites {
    register<JvmTestSuite>("timeConsuming") { targets.all { testTask { maxHeapSize = "4g" } } }
}

// Link 'main' dependency scopes similar as they are linked for 'test'
// https://docs.gradle.org/current/userguide/java_library_plugin.html#sec:java_library_configurations_graph
configurations {
    getByName("timeConsumingCompileOnly") { extendsFrom(configurations.compileOnly.get()) }
    getByName("timeConsumingImplementation") { extendsFrom(configurations.implementation.get()) }
    getByName("timeConsumingRuntimeOnly") { extendsFrom(configurations.runtimeOnly.get()) }
}
