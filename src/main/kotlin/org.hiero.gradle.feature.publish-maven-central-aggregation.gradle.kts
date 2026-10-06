// SPDX-License-Identifier: Apache-2.0
import java.time.Duration
import java.time.temporal.ChronoUnit

plugins {
    id("java")
    id("org.hiero.gradle.base.lifecycle")
    id("org.hiero.gradle.base.version")
    id("org.hiero.gradle.base.jpms-modules")
    id("com.gradleup.nmcp.aggregation")
}

@Suppress("UnstableApiUsage")
configurations {
    val published = dependencyScope("published")
    this.implementation { extendsFrom(published) }
    this.nmcpAggregation { extendsFrom(published) }
}

nmcpAggregation {
    // a 'test release' will be uploaded, but not automatically released
    val publishTestRelease =
        providers.gradleProperty("publishTestRelease").getOrElse("false").toBoolean()
    centralPortal {
        username = providers.environmentVariable("NEXUS_USERNAME")
        password = providers.environmentVariable("NEXUS_PASSWORD")
        publishingType = if (publishTestRelease) "USER_MANAGED" else "AUTOMATIC"
        validationTimeout =
            Duration.of(
                providers.gradleProperty("mavenCentralTimeout").getOrElse("60").toLong(),
                ChronoUnit.MINUTES,
            )
    }
}

if (version.toString().endsWith("-SNAPSHOT")) {
    tasks.publishAggregationToCentralPortal {
        dependsOn(tasks.nmcpPublishAggregationToCentralPortalSnapshots) // do snapshot publish
    }
    tasks.nmcpPublishAggregationToCentralPortal {
        enabled = false // skip normal publish
    }
}
