// SPDX-License-Identifier: Apache-2.0
import org.gradle.kotlin.dsl.support.serviceOf
import org.gradlex.javamodule.dependencies.initialization.JavaModulesExtension
import org.gradlex.javamodule.dependencies.initialization.RootPluginsExtension
import org.hiero.gradle.extensions.PluginVersionsExtensionCreateAction
import org.hiero.gradle.problems.ProblemReporter

plugins {
    id("org.gradlex.java-module-dependencies")
    id("org.hiero.gradle.feature.build-cache")
    id("org.hiero.gradle.feature.repositories")
    id("org.hiero.gradle.report.develocity")
}

val minGradleVersion = "9.1"

if (GradleVersion.current() < GradleVersion.version(minGradleVersion)) {
    serviceOf<ObjectFactory>()
        .newInstance<ProblemReporter>()
        .warn(
            "Wrong Gradle version",
            "The hiero plugins are not fully compatible with the current Gradle version",
            "gradle/wrapper/gradle-wrapper.properties",
            " Run: ./gradlew wrapper --gradle-version $minGradleVersion",
        )
}

configure<RootPluginsExtension> {
    // Global plugins, that are applied to the "root project" instead of "settings".
    // Having this here, we do not require a "build.gradle.kts" in the repository roots.
    id("org.hiero.gradle.base.lifecycle")
    id("org.hiero.gradle.feature.rust.root")
    id("org.hiero.gradle.feature.versioning")
    id("org.hiero.gradle.check.spotless")
    id("org.hiero.gradle.check.spotless-kotlin")
    id("org.hiero.gradle.check.spotless-markdown")
    id("org.hiero.gradle.check.spotless-misc")
    id("org.hiero.gradle.check.spotless-yaml")
}

// Allow projects inside a build to be addressed by dependency coordinates notation.
// https://docs.gradle.org/current/userguide/composite_builds.html#included_build_declaring_substitutions
// Some functionality of the 'java-module-dependencies' plugin relies on this.
includeBuild(".")

@Suppress("UnstableApiUsage")
configure<JavaModulesExtension> {
    if (layout.rootDirectory.dir("gradle/aggregation").asFile.isDirectory) {
        // Project to aggregate code coverage data for the whole repository into one report
        module("gradle/aggregation") {
            plugin("org.hiero.gradle.base.lifecycle")
            plugin("org.hiero.gradle.base.version")
            plugin("org.hiero.gradle.feature.aggregation")
            plugin("org.hiero.gradle.feature.publish-maven-central-aggregation")
            plugin("org.hiero.gradle.report.code-coverage")
            plugin("org.hiero.gradle.check.spotless")
            plugin("org.hiero.gradle.check.spotless-kotlin")
        }
    }
    if (layout.rootDirectory.dir("hiero-dependency-versions").asFile.isDirectory) {
        // "BOM" with versions of 3rd party dependencies
        versions("hiero-dependency-versions") {
            plugin("org.hiero.gradle.base.lifecycle")
            plugin("org.hiero.gradle.base.jpms-modules")
            plugin("org.hiero.gradle.check.spotless")
            plugin("org.hiero.gradle.check.spotless-kotlin")
            plugin("org.hiero.gradle.check.versions")
        }
    }
}

// Export the plugin versions to be used for dependencies when needed.
val pluginVersions =
    buildscript.configurations["classpath"].dependencies.associate { it.group!! to it.version!! }

@Suppress("UnstableApiUsage")
gradle.lifecycle.beforeProject(PluginVersionsExtensionCreateAction(pluginVersions))
