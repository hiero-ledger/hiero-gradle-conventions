// SPDX-License-Identifier: Apache-2.0
plugins {
    id("java")
    id("antlr")
    id("org.hiero.gradle.base.jpms-modules")
}

configurations {
    // Treat the ANTLR compiler as a separate tool that should not end up on the compile/runtime
    // classpath of our runtime.
    // https://github.com/gradle/gradle/issues/820
    api { setExtendsFrom(extendsFrom.filterNot { it == antlr.get() }) }
    // Get ANTLR version from 'hiero-dependency-versions'
    antlr { extendsFrom(configurations["internal"]) }
}

dependencies { antlr("org.antlr:antlr4") }
