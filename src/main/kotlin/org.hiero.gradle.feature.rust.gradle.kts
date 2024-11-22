// SPDX-License-Identifier: Apache-2.0
import org.hiero.gradle.extensions.CargoExtension
import org.hiero.gradle.extensions.CargoToolchain.*
import org.hiero.gradle.services.TaskLockService
import org.hiero.gradle.tasks.CargoBuildTask

plugins { id("java") }

val cargo = project.extensions.create<CargoExtension>("cargo")

cargo.targets(aarch64Darwin, aarch64Linux, x86Darwin, x86Linux, x86Windows)

// Cargo might do installation work, do not run in parallel:
tasks.withType<CargoBuildTask>().configureEach {
    usesService(
        gradle.sharedServices.registerIfAbsent("lock", TaskLockService::class) {
            maxParallelUsages = 1
        }
    )
}
