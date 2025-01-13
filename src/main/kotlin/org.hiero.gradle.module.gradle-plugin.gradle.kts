// SPDX-License-Identifier: Apache-2.0
plugins {
    id("java-library")
    id("org.hiero.gradle.feature.publish-maven-repository")
    id("org.hiero.gradle.feature.publish-gradle-plugin-portal")
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

tasks.check { dependsOn(tasks.jacocoTestReport) }

extraJavaModuleInfo {
    failOnMissingModuleInfo = false
    failOnAutomaticModules = false
}
