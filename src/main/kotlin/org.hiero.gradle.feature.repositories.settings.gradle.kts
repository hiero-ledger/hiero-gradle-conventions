// SPDX-License-Identifier: Apache-2.0
dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositories {
        maven("https://hyperledger.jfrog.io/artifactory/besu-maven") {
            content { includeGroupByRegex("org\\.hyperledger\\..*") }
        }
        maven("https://artifacts.consensys.net/public/maven/maven/") {
            content { includeGroupByRegex("tech\\.pegasys(\\..*)?") }
        }
        maven("https://jitpack.io") {
            content { includeModule("io.github.cdimascio", "java-dotenv") }
        }
        mavenCentral()
        maven("https://oss.sonatype.org/content/repositories/snapshots")
    }
}
