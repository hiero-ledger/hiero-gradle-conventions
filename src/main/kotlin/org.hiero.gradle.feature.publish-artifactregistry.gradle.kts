// SPDX-License-Identifier: Apache-2.0
plugins { id("maven-publish") }

if (
    gradle.startParameter.taskNames.any {
        it.startsWith("release") &&
            !it.contains("MavenCentral") &&
            !it.contains("PrereleaseChannel")
    }
) {
    // We apply the 'artifactregistry' plugin conditionally, as it does not support configuration
    // cache.
    // https://github.com/GoogleCloudPlatform/artifact-registry-maven-tools/issues/85
    apply(plugin = "com.google.cloud.artifactregistry.gradle-plugin")
}

publishing.repositories {
    maven("artifactregistry://us-maven.pkg.dev/swirlds-registry/maven-prerelease-channel") {
        name = "prereleaseChannel"
    }
    maven("artifactregistry://us-maven.pkg.dev/swirlds-registry/maven-develop-snapshots") {
        name = "developSnapshot"
    }
    maven("artifactregistry://us-maven.pkg.dev/swirlds-registry/maven-develop-daily-snapshots") {
        name = "developDailySnapshot"
    }
    maven("artifactregistry://us-maven.pkg.dev/swirlds-registry/maven-develop-commits") {
        name = "developCommit"
    }
    maven("artifactregistry://us-maven.pkg.dev/swirlds-registry/maven-adhoc-commits") {
        name = "adhocCommit"
    }
}

// Register one 'release*' task for each publishing repository
publishing.repositories.all {
    val ucName = name.replaceFirstChar { it.titlecase() }
    tasks.register("release$ucName") {
        group = "release"
        dependsOn(tasks.named("publishMavenPublicationTo${ucName}Repository"))
    }
}
