// SPDX-License-Identifier: Apache-2.0
import org.hiero.gradle.services.TaskLockService

plugins { id("java") }

// Tests that are resource sensitive (e.g. use Thread.sleep()) and thus need to run without anything
// in parallel.
@Suppress("UnstableApiUsage")
testing.suites {
    register<JvmTestSuite>("timingSensitive") {
        testType = "timing-sensitive"
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
