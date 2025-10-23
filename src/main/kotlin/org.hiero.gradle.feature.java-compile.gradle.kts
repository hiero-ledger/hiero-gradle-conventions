// SPDX-License-Identifier: Apache-2.0
import org.hiero.gradle.environment.EnvAccess
import org.hiero.gradle.problems.ProblemReporter

plugins {
    id("java")
    id("org.gradlex.reproducible-builds")
}

@Suppress("UnstableApiUsage") val rootDir = project.isolated.rootProject.projectDirectory
val versions = EnvAccess.toolchainVersions(rootDir, providers, objects)

val currentJavaVersion = providers.systemProperty("java.version").get()
val fullJavaVersion =
    versions
        .getting("jdk")
        .orElse(
            provider {
                objects
                    .newInstance<ProblemReporter>()
                    .warn(
                        "Java Version not Pinned",
                        "No 'jdk' version pinned (using: $currentJavaVersion)",
                        "gradle/toolchain-versions.properties",
                        "Add jdk=$currentJavaVersion to gradle/toolchain-versions.properties",
                    )
                currentJavaVersion
            }
        )
        .get()
val majorJavaVersion = JavaVersion.toVersion(fullJavaVersion)

if (currentJavaVersion != fullJavaVersion) {
    objects
        .newInstance<ProblemReporter>()
        .warn(
            "Wrong Java Version",
            "Gradle runs with Java $currentJavaVersion. This project works best running with Java $fullJavaVersion",
            "gradle/toolchain-versions.properties",
            "Point at Java $fullJavaVersion installation: " +
                "JAVA_HOME and/or PATH (command line), " +
                "'Gradle JVM' in 'Gradle Settings' (IntelliJ)",
        )
}

java {
    sourceCompatibility = majorJavaVersion
    targetCompatibility = majorJavaVersion
}

tasks.withType<JavaCompile>().configureEach {
    // Track the full Java version as input (e.g. 17.0.3 vs. 17.0.9).
    // By default, Gradle only tracks the major version as defined in the toolchain (e.g. 17).
    // Since the full version is encoded in 'module-info.class' files, it should be tracked as
    // it otherwise leads to wrong build cache hits.
    inputs.property("fullJavaVersion", currentJavaVersion)
    // make the Java compiler add the Module's version to the module-info.class file
    options.javaModuleVersion = provider { project.version as String }
}

tasks.jar {
    manifest {
        attributes["Implementation-Title"] = project.name
        attributes["Implementation-Version"] = project.version
    }
}

sourceSets.all {
    // 'assemble' compiles all sources, including all test sources
    tasks.assemble { dependsOn(tasks.named(classesTaskName)) }
}
