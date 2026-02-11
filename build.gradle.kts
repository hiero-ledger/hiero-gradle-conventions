// SPDX-License-Identifier: Apache-2.0
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
    implementation(platform("org.jetbrains.kotlin:kotlin-bom:${embeddedKotlinVersion}"))

    api("com.diffplug.spotless:spotless-lib")
    api("com.gradleup.shadow:shadow-gradle-plugin:9.1.0")

    implementation("com.adarshr:gradle-test-logger-plugin:4.0.0")
    implementation("com.autonomousapps:dependency-analysis-gradle-plugin:3.5.1")
    implementation("com.diffplug.spotless:spotless-plugin-gradle:8.2.1")
    implementation("com.github.node-gradle:gradle-node-plugin:7.1.0") // install NPM for prettier
    implementation("com.google.protobuf:protobuf-gradle-plugin:0.9.6")
    implementation("com.gradle.publish:plugin-publish-plugin:2.0.0")
    implementation("com.gradle:develocity-gradle-plugin:4.3.2")
    implementation("com.gradleup.nmcp:nmcp:1.2.1")
    implementation("me.champeau.jmh:jmh-gradle-plugin:0.7.3")
    implementation("net.swiftzer.semver:semver:2.1.0")
    implementation("org.gradlex:extra-java-module-info:1.14")
    implementation("org.gradlex:java-module-dependencies:1.12")
    implementation("org.gradlex:java-module-testing:1.8")
    implementation("org.gradlex:jvm-dependency-conflict-resolution:2.5")

    runtimeOnly("com.gradle:common-custom-user-data-gradle-plugin:2.4.0")
    runtimeOnly(
        "gradle.plugin.com.google.cloud.artifactregistry:artifactregistry-gradle-plugin:2.2.2"
    )
    runtimeOnly("io.freefair.gradle:maven-plugin:9.2.0") // for POM validation
    runtimeOnly("org.gradlex:reproducible-builds:1.1")

    testImplementation("org.assertj:assertj-core:3.27.7")
    testImplementation("org.junit.jupiter:junit-jupiter-api:6.0.2")
    testImplementation("org.junit.jupiter:junit-jupiter-params")
}

// https://github.com/autonomousapps/dependency-analysis-gradle-plugin/pull/1640
jvmDependencyConflicts {
    patch.module("com.autonomousapps:dependency-analysis-gradle-plugin") {
        removeDependency("javax.inject:javax.inject")
    }
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

tasks.wrapper {
    doLast {
        // ensure the Gradle version the plugins are built with is defined as the minimal version
        // for users
        val buildPluginFile = File("src/main/kotlin/org.hiero.gradle.build.settings.gradle.kts")
        buildPluginFile.writeText(
            buildPluginFile
                .readText()
                .replace(
                    Regex("minGradleVersion = \".+\""),
                    "minGradleVersion = \"${GradleVersion.current().version}\"",
                )
        )
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
    val delimiter = "^(?!\\/\\/ SPDX)."
    kotlin {
        ktfmt().kotlinlangStyle()
        targetExclude("build/**")
        licenseHeader(header, delimiter)
    }
}
