// SPDX-License-Identifier: Apache-2.0
import org.hiero.gradle.services.TaskLockService

plugins { id("java") }

// Test functionally correct behavior under stress/loads with many repeated iterations.
@Suppress("UnstableApiUsage")
testing.suites {
    register<JvmTestSuite>("hammer") {
        testType = "hammer"
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
