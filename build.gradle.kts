// SPDX-License-Identifier: Apache-2.0
import org.gradle.kotlin.dsl.repositories
import org.hiero.gradle.environment.EnvAccess
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
    id("org.hiero.gradle.module.gradle-plugin")
    `kotlin-dsl`
}

group = "org.hiero.gradle"

description = "Gradle convention plugins used by Hiero projects"

dependencies {
    implementation("com.adarshr:gradle-test-logger-plugin:4.0.0")
    implementation("com.autonomousapps:dependency-analysis-gradle-plugin:2.19.0")
    implementation("com.diffplug.spotless:spotless-plugin-gradle:7.1.0")
    implementation("com.github.node-gradle:gradle-node-plugin:7.1.0") // install NPM for prettier
    implementation("com.google.protobuf:protobuf-gradle-plugin:0.9.5")
    implementation("com.gradle.publish:plugin-publish-plugin:1.3.1")
    implementation("com.gradle:develocity-gradle-plugin:4.1")
    implementation("com.gradleup.nmcp:nmcp:1.0.1")
    implementation("com.gradleup.shadow:shadow-gradle-plugin:8.3.8")
    implementation(
        "gradle.plugin.com.google.cloud.artifactregistry:artifactregistry-gradle-plugin:2.2.2"
    )
    implementation("io.freefair.gradle:maven-plugin:8.14") // for POM validation
    implementation("me.champeau.jmh:jmh-gradle-plugin:0.7.3")
    implementation("net.swiftzer.semver:semver:2.1.0")
    implementation("org.gradlex:extra-java-module-info:1.13")
    implementation("org.gradlex:java-module-dependencies:1.9.2")
    implementation("org.gradlex:java-module-testing:1.7")
    implementation("org.gradlex:jvm-dependency-conflict-resolution:2.4")
    implementation("org.gradlex:reproducible-builds:1.0")

    implementation(platform("org.jetbrains.kotlin:kotlin-bom:${embeddedKotlinVersion}"))

    testImplementation(platform("org.junit:junit-bom:5.13.3"))
    testImplementation("org.assertj:assertj-core:3.27.3")
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
        tags = listOf("hiero", "conventions", "java", "modules", "jpms")
    }

    plugins.configureEach { displayName = name }
}

publishing {
    repositories.maven {
        name = "MavenCentralSnapshots"
        url = uri("https://central.sonatype.com/repository/maven-snapshots")
        credentials {
            username = providers.environmentVariable("NEXUS_USERNAME").orNull
            password = providers.environmentVariable("NEXUS_PASSWORD").orNull
        }
    }
}

tasks.test {
    // If success, delete all test projects
    doLast { File("build/test-projects").deleteRecursively() }
}

val env = EnvAccess.toolchainVersions(layout.projectDirectory, providers, objects)
val fullJavaVersion = env.getting("jdk").get()
val majorJavaVersion = JavaVersion.toVersion(fullJavaVersion).majorVersion

tasks.withType<KotlinJvmCompile>().configureEach {
    compilerOptions { jvmTarget = JvmTarget.valueOf("JVM_$majorJavaVersion") }
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
