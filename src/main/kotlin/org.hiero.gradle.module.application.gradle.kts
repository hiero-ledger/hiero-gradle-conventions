// SPDX-License-Identifier: Apache-2.0
plugins {
    id("application")
    id("jacoco")
    id("org.hiero.gradle.base.jpms-modules")
    id("org.hiero.gradle.base.lifecycle")
    id("org.hiero.gradle.base.version")
    id("org.hiero.gradle.check.dependencies")
    id("org.hiero.gradle.check.javac-lint")
    id("org.hiero.gradle.check.spotless")
    id("org.hiero.gradle.check.spotless-java")
    id("org.hiero.gradle.check.spotless-kotlin")
    id("org.hiero.gradle.feature.git-properties-file")
    id("org.hiero.gradle.feature.java-compile")
    id("org.hiero.gradle.feature.java-doc")
    id("org.hiero.gradle.feature.java-execute")
    id("org.hiero.gradle.feature.test")
    id("org.hiero.gradle.report.test-logger")
}

// Make the Jar itself executable by setting the 'Main-Class' manifest attribute.
tasks.jar { manifest { attributes("Main-Class" to application.mainClass) } }

// The 'application' plugin activates the following tasks as part of 'assemble'.
// As we do not use these results right now, disable them:
tasks.startScripts { enabled = false }

tasks.distTar { enabled = false }

tasks.distZip { enabled = false }
