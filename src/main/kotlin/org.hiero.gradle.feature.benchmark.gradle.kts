// SPDX-License-Identifier: Apache-2.0
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

tasks.jmh {
    group = "build"
    outputs.upToDateWhen { false }
}

tasks.withType<JMHTask>().configureEach {
    group = "jmh"
    jarArchive = tasks.jmhJar.flatMap { it.archiveFile }
    jvm = javaToolchains.launcherFor(java.toolchain).map { it.executablePath }.get().asFile.path
}

tasks.jmhJar { manifest { attributes(mapOf("Multi-Release" to true)) } }

// Disable module Jar patching for the JMH runtime classpath.
extraJavaModuleInfo { deactivate(sourceSets.jmh) }
