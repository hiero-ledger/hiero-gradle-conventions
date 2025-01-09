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

        ivy("https://nodejs.org/dist") { // for 'com.github.node-gradle.node' plugin
            patternLayout { artifact("v[revision]/[artifact](-v[revision]-[classifier]).[ext]") }
            metadataSources { artifact() }
            content { includeModule("org.nodejs", "node") }
        }

        mavenCentral()
        maven("https://oss.sonatype.org/content/repositories/snapshots")
    }

    @Suppress("UnstableApiUsage")
    repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
}
