// SPDX-License-Identifier: Apache-2.0
import org.gradle.kotlin.dsl.support.serviceOf
import org.hiero.gradle.environment.EnvAccess
import org.hiero.gradle.problems.ProblemReporter

if (!gradle.startParameter.isBuildCacheEnabled) {
    serviceOf<ObjectFactory>()
        .newInstance<ProblemReporter>()
        .warn(
            "Build Cache Disabled",
            "",
            "gradle.properties",
            "Add org.gradle.caching=true to gradle.properties",
        )
}

buildCache {
    remote<HttpBuildCache> {
        val remoteCacheEnabled =
            providers.gradleProperty("org.hiero.gradle.remote-cache").getOrElse("true").toBoolean()
        url = uri("https://cache.gradle.hedera.svcs.eng.swirldslabs.io/cache/")

        isUseExpectContinue = true
        isEnabled = remoteCacheEnabled && !gradle.startParameter.isOffline

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
