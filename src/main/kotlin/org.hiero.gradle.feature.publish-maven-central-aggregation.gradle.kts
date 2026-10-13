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

// In case of SNAPSHOT, do nothing as the upload is done directly by each module individually
val nonSnapshotRelease = !version.toString().endsWith("-SNAPSHOT")

configurations {
    val published = dependencyScope("published")
    this.implementation { extendsFrom(published.get()) }
    this.nmcpAggregation { extendsFrom(published.get()) }
}

nmcpAggregation {
    // a 'private release' is uploaded and validated, but not automatically made public.
    // It can be made public later through 'nmcpPublishDeployment -PnmcpDeploymentId=<id>'.
    // 'publishTestRelease' is the previous name of 'publishPrivateRelease' and is kept as alias.
    val publishPrivateRelease =
        providers
            .gradleProperty("publishPrivateRelease")
            .orElse(providers.gradleProperty("publishTestRelease"))
            .getOrElse("false")
            .toBoolean()
    centralPortal {
        username = providers.environmentVariable("NEXUS_USERNAME")
        password = providers.environmentVariable("NEXUS_PASSWORD")
        publishingType = if (publishPrivateRelease) "USER_MANAGED" else "AUTOMATIC"
        validationTimeout =
            Duration.of(
                providers.gradleProperty("mavenCentralTimeout").getOrElse("60").toLong(),
                ChronoUnit.MINUTES,
            )
    }
}

tasks.named("nmcpPublishAggregationToCentralPortal") {
    enabled = nonSnapshotRelease
    group = "release"
}

tasks.named("nmcpPublishDeployment") {
    group = "release"
    description =
        "Make a private release, previously uploaded with -PpublishPrivateRelease=true, public. " +
            "Requires -PnmcpDeploymentId=<id>."
}
