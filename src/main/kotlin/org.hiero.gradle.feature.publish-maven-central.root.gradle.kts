// SPDX-License-Identifier: Apache-2.0
plugins {
    id("org.hiero.gradle.base.lifecycle")
    id("io.github.gradle-nexus.publish-plugin")
}

nexusPublishing {
    val s01SonatypeHost = providers.gradleProperty("s01SonatypeHost").getOrElse("false").toBoolean()
    val versionTxt = layout.projectDirectory.file("version.txt")

    packageGroup = providers.gradleProperty("publishingPackageGroup")
    useStaging =
        providers.fileContents(versionTxt).asText.map { !it.contains("-SNAPSHOT") }.orElse(false)

    repositories {
        sonatype {
            username = providers.environmentVariable("NEXUS_USERNAME")
            password = providers.environmentVariable("NEXUS_PASSWORD")
            if (s01SonatypeHost) {
                nexusUrl = uri("https://s01.oss.sonatype.org/service/local/")
                snapshotRepositoryUrl =
                    uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
            }
        }
    }
}

tasks.named("closeSonatypeStagingRepository") {
    // The publishing of all components to Maven Central is automatically done before close (which
    // is done before release).
    dependsOn(subprojects.map { ":${it.name}:releaseMavenCentral" })
}

tasks.named("releaseMavenCentral") {
    val publishTestRelease =
        providers.gradleProperty("publishTestRelease").getOrElse("false").toBoolean()
    if (publishTestRelease) {
        // Test release: only close staging repo but do not auto-release
        dependsOn(tasks.named("closeSonatypeStagingRepository"))
    } else {
        dependsOn(tasks.closeAndReleaseStagingRepository)
    }
}

tasks.register("releaseMavenCentralSnapshot") {
    group = "release"
    dependsOn(subprojects.map { ":${it.name}:releaseMavenCentral" })
}
