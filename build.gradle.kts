// SPDX-License-Identifier: Apache-2.0
plugins {
    id("org.hiero.gradle.module.gradle-plugin")
    `kotlin-dsl`
}

group = "org.hiero.gradle"

description = "Gradle convention plugins used by Hiero projects"

dependencies {
    implementation("com.adarshr:gradle-test-logger-plugin:4.0.0")
    implementation("com.autonomousapps:dependency-analysis-gradle-plugin:2.6.1")
    implementation("com.diffplug.spotless:spotless-plugin-gradle:7.0.1")
    implementation("com.google.protobuf:protobuf-gradle-plugin:0.9.4")
    implementation("com.gradle:develocity-gradle-plugin:3.19")
    implementation("com.gradle.publish:plugin-publish-plugin:1.3.0")
    implementation(
        "gradle.plugin.com.google.cloud.artifactregistry:artifactregistry-gradle-plugin:2.2.2"
    )
    implementation("io.freefair.gradle:maven-plugin:8.11") // for POM validation
    implementation("io.github.gradle-nexus:publish-plugin:2.0.0")
    implementation("me.champeau.jmh:jmh-gradle-plugin:0.7.2")
    implementation("net.swiftzer.semver:semver:2.0.0")
    implementation("org.gradlex:extra-java-module-info:1.9")
    implementation("org.gradlex:java-module-dependencies:1.8")
    implementation("org.gradlex:jvm-dependency-conflict-resolution:2.1.2")
    implementation("org.gradlex:reproducible-builds:1.0")

    implementation(platform("org.jetbrains.kotlin:kotlin-bom:${embeddedKotlinVersion}"))

    testImplementation(platform("org.junit:junit-bom:5.11.4"))
    testImplementation("org.assertj:assertj-core:3.27.2")
    testImplementation("org.junit.jupiter:junit-jupiter-params")
}

gradlePlugin {
    website = "https://github.com/hiero-ledger/hiero-gradle-conventions"
    vcsUrl = "https://github.com/hiero-ledger/hiero-gradle-conventions"
    plugins.configureEach {
        val descriptionFile = layout.projectDirectory.file("src/main/descriptions/${id}.txt")
        description =
            providers
                .fileContents(descriptionFile)
                .asText
                .orElse(
                    provider {
                        throw RuntimeException(
                            "File not found ${descriptionFile.asFile.absolutePath}"
                        )
                    }
                )
                .get()
                .trim()
        @Suppress("UnstableApiUsage")
        tags = listOf("hiero", "conventions", "java", "modules", "jpms")
    }

    plugins.configureEach { displayName = name }
}

tasks.test {
    // If success, delete all test projects
    doLast { File("build/test-projects").deleteRecursively() }
}

spotless {
    // Format '*.kts' and '*.kt' in 'src/main/kotlin'
    val header = "// SPDX-License-Identifier: Apache-2.0\n"
    val delimiter =
        "(import|package|plugins|pluginManagement|dependencyResolutionManagement|repositories|tasks|allprojects|subprojects|buildCache|version)"
    kotlin {
        ktfmt().kotlinlangStyle()
        targetExclude("build/**")
        licenseHeader(header, delimiter)
    }
}
