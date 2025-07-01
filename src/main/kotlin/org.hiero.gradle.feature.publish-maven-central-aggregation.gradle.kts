// SPDX-License-Identifier: Apache-2.0
plugins {
    id("java")
    id("org.hiero.gradle.base.lifecycle")
    id("org.hiero.gradle.base.version")
    id("org.hiero.gradle.base.jpms-modules")
    id("com.gradleup.nmcp.aggregation")
}

// In case of SNAPSHOT, do nothing as the upload is done directly by each module individually
val nonSnapshotRelease = !version.toString().endsWith("-SNAPSHOT")

@Suppress("UnstableApiUsage")
configurations {
    val published = dependencyScope("published")
    this.implementation { extendsFrom(published.get()) }
    this.nmcpAggregation { extendsFrom(published.get()) }
}

nmcpAggregation {
    // a 'test release' will be uploaded, but not automatically released
    val publishTestRelease =
        providers.gradleProperty("publishTestRelease").getOrElse("false").toBoolean()
    centralPortal {
        username = providers.environmentVariable("NEXUS_USERNAME")
        password = providers.environmentVariable("NEXUS_PASSWORD")
        publishingType = if (publishTestRelease) "USER_MANAGED" else "AUTOMATIC"
    }
}

tasks.named("publishAggregationToCentralPortal") {
    enabled = nonSnapshotRelease
    group = "release"
}
