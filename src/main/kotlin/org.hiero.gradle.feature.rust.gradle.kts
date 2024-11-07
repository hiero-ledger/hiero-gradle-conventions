/*
 * Copyright (C) 2022-2024 Hiero a Series of LF Projects, LLC
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
