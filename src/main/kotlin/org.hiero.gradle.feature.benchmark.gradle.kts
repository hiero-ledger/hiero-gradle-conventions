// SPDX-License-Identifier: Apache-2.0
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import me.champeau.jmh.JMHTask

plugins {
    id("java")
    id("org.hiero.gradle.base.jpms-modules")
    id("me.champeau.jmh")
}

jmh {
    jmhVersion = "1.37"
    includeTests = false
    // Filter JMH tests from command line via -PjmhTests=...
    val commandLineIncludes = providers.gradleProperty("jmhTests")
    if (commandLineIncludes.isPresent) {
        includes.add(commandLineIncludes.get())
    }
}

dependencies {
    // Required for the JMH IDEA plugin:
    // https://plugins.jetbrains.com/plugin/7529-jmh-java-microbenchmark-harness
    jmhAnnotationProcessor("org.openjdk.jmh:jmh-generator-annprocess:${jmh.jmhVersion.get()}")
}

val jmhJarWithMergedServiceFiles =
    tasks.register<ShadowJar>("jmhJarWithMergedServiceFiles") {
        archiveClassifier.set("jmh-merged")
        isZip64 = true
        manifest { attributes("Main-Class" to "org.openjdk.jmh.Main", "Multi-Release" to "true") }
        from(project.sourceSets.jmh.get().output)
        configurations = setOf(project.configurations.jmhRuntimeClasspath.get())

        // For entries for which duplications is expected, add only the first (and EXCLUDE others)
        filesMatching("*") { duplicatesStrategy = DuplicatesStrategy.EXCLUDE }
        filesMatching("META-INF/*") { duplicatesStrategy = DuplicatesStrategy.EXCLUDE }
        filesMatching("META-INF/helidon/*") { duplicatesStrategy = DuplicatesStrategy.EXCLUDE }

        // For service registrations include duplicates as they are merged into one
        // https://gradleup.com/shadow/changes/#migration-example
        filesMatching("META-INF/services/**") { duplicatesStrategy = DuplicatesStrategy.INCLUDE }
        mergeServiceFiles()

        // Standard excludes (same as tasks.shadowJar has by default)
        exclude(
            "META-INF/INDEX.LIST",
            "META-INF/*.SF",
            "META-INF/*.DSA",
            "META-INF/*.RSA",
            "META-INF/versions/**/module-info.class",
            "module-info.class",
            "META-INF/versions/**/OSGI-INF/MANIFEST.MF",
            "META-INF/maven/**",
        )
    }

tasks.withType<JMHTask>().configureEach {
    group = "jmh"
    outputs.upToDateWhen { false }
    jarArchive = jmhJarWithMergedServiceFiles.flatMap { it.archiveFile }
    jvm = javaToolchains.launcherFor(java.toolchain).map { it.executablePath }.get().asFile.path
}

// Disable module Jar patching for the JMH runtime classpath.
extraJavaModuleInfo { deactivate(sourceSets.jmh) }
