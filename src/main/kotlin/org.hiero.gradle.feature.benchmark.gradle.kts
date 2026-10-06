// SPDX-License-Identifier: Apache-2.0
import me.champeau.jmh.JMHTask
import org.gradlex.javamodule.packaging.tasks.FatModuleJar

plugins {
    id("java")
    id("me.champeau.jmh")
    id("org.gradlex.java-module-packaging")
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

val jmhFatModuleJar =
    tasks.register<FatModuleJar>("jmhFatModuleJar") {
        mainModule = "jmh.core"
        mainClass = "org.openjdk.jmh.Main"
        modulePath.from(configurations.jmhRuntimeClasspath)
        archiveClassifier.set("jmh-fat")
        from(project.sourceSets.jmh.get().output)
    }

tasks.withType<JMHTask>().configureEach {
    group = "jmh"
    outputs.upToDateWhen { false }
    jarArchive = jmhFatModuleJar.flatMap { it.archiveFile }
    jvm = javaToolchains.launcherFor(java.toolchain).map { it.executablePath }.get().asFile.path
}
