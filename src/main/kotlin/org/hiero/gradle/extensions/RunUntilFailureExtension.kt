package org.hiero.gradle.extensions

import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property

abstract class RunUntilFailureExtension {
    /**
     * The name of the test task to run repeatedly.
     * Defaults to "test".
     */
    abstract val testTaskName: Property<String>

    /**
     * Maximum number of times to run the test task.
     * A value of 0 means run indefinitely until failure.
     * Defaults to 0 (unlimited).
     */
    abstract val maxRetries: Property<Int>

    /**
     * Optional test filters forwarded to the target task via `--tests`.
     * Supports the same patterns as Gradle's built-in test filtering:
     *   - fully-qualified class:      "com.example.MyTest"
     *   - specific method:            "com.example.MyTest.myMethod"
     *   - wildcard:                   "com.example.*"
     *   - simple class name:          "MyTest"
     *
     * Example (build.gradle.kts):
     *   runUntilFailure {
     *       testFilters.set(listOf("com.example.FlakyTest.flakyMethod"))
     *   }
     */
    abstract val testFilters: ListProperty<String>

    init {
        testTaskName.convention("test")
        maxRetries.convention(0)
        testFilters.convention(emptyList())
    }
}
