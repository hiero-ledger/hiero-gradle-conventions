// SPDX-License-Identifier: Apache-2.0
import org.hiero.gradle.environment.EnvAccess

if (!gradle.startParameter.isBuildCacheEnabled) {
    logger.warn(
        "WARN: Build cache disabled" + "\n - Add org.gradle.caching=true to gradle.properties"
    )
}

buildCache {
    remote<HttpBuildCache> {
        url = uri("https://cache.gradle.hedera.svcs.eng.swirldslabs.io/cache/")

        isUseExpectContinue = true
        isEnabled = !gradle.startParameter.isOffline

        val ci = EnvAccess.isCiServer(providers)
        val gradleCacheUsername = providers.environmentVariable("GRADLE_CACHE_USERNAME")
        val gradleCachePassword = providers.environmentVariable("GRADLE_CACHE_PASSWORD")
        if (ci && gradleCacheUsername.isPresent && gradleCachePassword.isPresent) {
            isPush = true
            credentials {
                username = gradleCacheUsername.get()
                password = gradleCachePassword.get()
            }
        }
    }
}
