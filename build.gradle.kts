/*
 * Copyright (C) 2022-2024 Hiero a Series of LF Projects, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

plugins {
    `kotlin-dsl`
    id("signing")
    id("com.gradle.plugin-publish") version "1.3.0"
    id("com.diffplug.spotless") version "6.25.0"
}

version = "0.1.0"

group = "org.hiero"

description = "Gradle convention plugins used by Hiero projects"

java { toolchain.languageVersion = JavaLanguageVersion.of(17) }

dependencies {
    implementation("com.adarshr:gradle-test-logger-plugin:4.0.0")
    implementation("com.autonomousapps:dependency-analysis-gradle-plugin:2.5.0")
    implementation("com.diffplug.spotless:spotless-plugin-gradle:6.25.0")
    implementation("com.google.protobuf:protobuf-gradle-plugin:0.9.4")
    implementation("com.gradle:develocity-gradle-plugin:3.18.1")
    implementation("com.gradle.publish:plugin-publish-plugin:1.3.0")
    implementation(
        "gradle.plugin.com.google.cloud.artifactregistry:artifactregistry-gradle-plugin:2.2.2"
    )
    implementation("io.freefair.gradle:maven-plugin:8.11") // for POM validation
    implementation("io.github.gradle-nexus:publish-plugin:1.3.0")
    implementation("me.champeau.jmh:jmh-gradle-plugin:0.7.2")
    implementation("net.swiftzer.semver:semver:2.0.0")
    implementation("org.gradlex:extra-java-module-info:1.9")
    implementation("org.gradlex:java-module-dependencies:1.8")
    implementation("org.gradlex:jvm-dependency-conflict-resolution:2.1.2")
    implementation("org.gradlex:reproducible-builds:1.0")
}

gradlePlugin {
    website = "https://github.com/hiero-ledger/hiero-gradle-conventions"
    vcsUrl = "https://github.com/hiero-ledger/hiero-gradle-conventions"
    plugins.configureEach {
        description = project.description
        @Suppress("UnstableApiUsage")
        tags = listOf("conventions", "java", "modules", "jpms")
    }

    plugins.configureEach { displayName = name }
}

publishing.publications.withType<MavenPublication>().configureEach {
    pom {
        url = "https://hiero.org/"
        inceptionYear = "2024"
        description = project.description
        organization {
            name = "Hiero - a Linux Foundation Decentralized Trust project"
            url = "https://hiero.org/"
        }

        val repoName = project.name
        issueManagement {
            system = "GitHub"
            url = "https://github.com/hiero-ledger/$repoName/issues"
        }

        licenses {
            license {
                name = "Apache License, Version 2.0"
                url = "https://raw.githubusercontent.com/hiero-ledger/$repoName/main/LICENSE"
            }
        }

        scm {
            connection = "scm:git:git://github.com/hiero-ledger/$repoName.git"
            developerConnection = "scm:git:ssh://github.com:hiero-ledger/$repoName.git"
            url = "https://github.com/hiero-ledger/$repoName"
        }

        developers {
            developer {
                name = "Release Engineering Team"
                email = "release-engineering@hiero.org"
                organization = "Hiero - a Linux Foundation Decentralized Trust project"
                organizationUrl = "https://hiero.org/"
            }
        }
    }
}

val publishSigningEnabled =
    providers.gradleProperty("publishSigningEnabled").getOrElse("false").toBoolean()

if (publishSigningEnabled) {
    signing {
        sign(publishing.publications)
        useGpgCmd()
    }
}

testing {
    @Suppress("UnstableApiUsage")
    suites.named<JvmTestSuite>("test") {
        useJUnitJupiter()
        dependencies {
            implementation("org.junit.jupiter:junit-jupiter-params")
            implementation("org.assertj:assertj-core:3.26.3")
        }
        // If success, delete all test projects
        targets.all { testTask { doLast { File("build/test-projects").deleteRecursively() } } }
    }
}

spotless {
    val header =
        """
           /*
            * Copyright (C) ${'$'}YEAR Hiero a Series of LF Projects, LLC
            *
            * Licensed under the Apache License, Version 2.0 (the "License");
            * you may not use this file except in compliance with the License.
            * You may obtain a copy of the License at
            *
            *      http://www.apache.org/licenses/LICENSE-2.0
            *
            * Unless required by applicable law or agreed to in writing, software
            * distributed under the License is distributed on an "AS IS" BASIS,
            * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
            * See the License for the specific language governing permissions and
            * limitations under the License.
            */${"\n\n"}
        """
            .trimIndent()
    val top =
        "(import|package|plugins|pluginManagement|dependencyResolutionManagement|repositories|tasks|allprojects|subprojects|buildCache|version)"

    kotlinGradle {
        ktfmt().kotlinlangStyle()
        licenseHeader(header, top).updateYearWithLatest(true)
    }
    kotlin {
        ktfmt().kotlinlangStyle()
        targetExclude("build/**")
        licenseHeader(header, top).updateYearWithLatest(true)
    }
}
