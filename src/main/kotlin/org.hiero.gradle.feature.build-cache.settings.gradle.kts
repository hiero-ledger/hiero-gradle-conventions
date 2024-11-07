/*
 * Copyright (C) 2024 Hiero a Series of LF Projects, LLC
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

buildCache {
    remote<HttpBuildCache> {
        url = uri("https://cache.gradle.hedera.svcs.eng.swirldslabs.io/cache/")

        isUseExpectContinue = true
        isEnabled = !gradle.startParameter.isOffline

        val isCiServer = providers.environmentVariable("CI").getOrElse("false").toBoolean()
        val gradleCacheUsername = providers.environmentVariable("GRADLE_CACHE_USERNAME")
        val gradleCachePassword = providers.environmentVariable("GRADLE_CACHE_PASSWORD")
        if (isCiServer && gradleCacheUsername.isPresent && gradleCachePassword.isPresent) {
            isPush = true
            credentials {
                username = gradleCacheUsername.get()
                password = gradleCachePassword.get()
            }
        }
    }
}
