// SPDX-License-Identifier: Apache-2.0
import org.gradle.kotlin.dsl.support.serviceOf

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
    resolvable("transitiveNmcpAggregation") {
        extendsFrom(published.get())
        attributes {
            attribute(Attribute.of("com.gradleup.nmcp", Named::class.java), objects.named("bundle"))
            attribute(Usage.USAGE_ATTRIBUTE, objects.named("nmcp"))
        }
    }
}

tasks.zipAggregation {
    enabled = nonSnapshotRelease

    val archiveOperations = serviceOf<ArchiveOperations>()
    from(
        configurations["transitiveNmcpAggregation"]
            .incoming
            .artifactView {
                this.lenient(true)
                this.componentFilter { it is ProjectComponentIdentifier }
            }
            .files
            .elements
            .map { it.map { zip -> archiveOperations.zipTree(zip) } }
    )
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

tasks.publishAggregationToCentralPortal {
    enabled = nonSnapshotRelease
    group = "release"
}
