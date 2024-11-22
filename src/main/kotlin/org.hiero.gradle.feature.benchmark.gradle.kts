// SPDX-License-Identifier: Apache-2.0
import me.champeau.jmh.JMHTask

plugins { id("me.champeau.jmh") }

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

tasks.jmh { outputs.upToDateWhen { false } }

tasks.withType<JMHTask>().configureEach {
    group = "jmh"
    jarArchive = tasks.jmhJar.flatMap { it.archiveFile }
    jvm = javaToolchains.launcherFor(java.toolchain).map { it.executablePath }.get().asFile.path
}

tasks.jmhJar { manifest { attributes(mapOf("Multi-Release" to true)) } }

configurations {
    // Disable module Jar patching for the JMH runtime classpath.
    // The way the JMH plugin interacts with this in the 'jmhJar' task triggers this Gradle issue:
    // https://github.com/gradle/gradle/issues/27372
    // And since 'jmhJar' builds a fat jar, module information is not needed here anyway.
    val javaModule = Attribute.of("javaModule", Boolean::class.javaObjectType)
    jmhRuntimeClasspath { attributes { attribute(javaModule, false) } }
    jmhCompileClasspath { attributes { attribute(javaModule, false) } }
    jmhAnnotationProcessor { attributes { attribute(javaModule, false) } }
}
