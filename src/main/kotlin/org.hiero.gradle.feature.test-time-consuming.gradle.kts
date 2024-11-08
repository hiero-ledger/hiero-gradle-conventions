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

plugins { id("java") }

// Tests that could be in the default "test" set but take more than 100ms to execute.
@Suppress("UnstableApiUsage")
testing.suites {
    register<JvmTestSuite>("timeConsuming") {
        testType = "timing-consuming"
        targets.all { testTask { maxHeapSize = "4g" } }
    }
}
