// SPDX-License-Identifier: Apache-2.0
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import me.champeau.jmh.JMHTask

plugins {
    id("java")
    id("org.hiero.gradle.base.jpms-modules")
    id("org.hiero.gradle.feature.shadow")
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
        manifest.attributes("Main-Class" to "org.openjdk.jmh.Main")
        from(project.sourceSets.jmh.get().output)
        configurations = setOf(project.configurations.jmhRuntimeClasspath.get())
        exclude("module-info.class", "META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA")
    }

tasks.withType<JMHTask>().configureEach {
    group = "jmh"
    outputs.upToDateWhen { false }
    jarArchive = jmhJarWithMergedServiceFiles.flatMap { it.archiveFile }
    jvm = javaToolchains.launcherFor(java.toolchain).map { it.executablePath }.get().asFile.path
}

// Disable module Jar patching for the JMH runtime classpath.
extraJavaModuleInfo { deactivate(sourceSets.jmh) }
