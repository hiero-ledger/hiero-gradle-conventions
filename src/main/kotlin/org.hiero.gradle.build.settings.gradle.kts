/*
 * Copyright (C) 2022-2024 Hiero a Series of LF Projects, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.gradlex.javamodule.dependencies.initialization.JavaModulesExtension
import org.gradlex.javamodule.dependencies.initialization.RootPluginsExtension

plugins {
    id("org.gradlex.java-module-dependencies")
    id("org.hiero.gradle.feature.build-cache")
    id("org.hiero.gradle.feature.repositories")
    id("org.hiero.gradle.report.develocity")
}

configure<RootPluginsExtension> {
    // Global plugins, that are applied to the "root project" instead of "settings".
    // Having this here, we do not require a "build.gradle.kts" in the repository roots.
    id("org.hiero.gradle.base.lifecycle")
    id("org.hiero.gradle.feature.publish-maven-central.root")
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
            plugin("java")
            plugin("org.hiero.gradle.base.jpms-modules")
        }
    }
    if (layout.rootDirectory.dir("hiero-dependency-versions").asFile.isDirectory) {
        // "BOM" with versions of 3rd party dependencies
        versions("hiero-dependency-versions")
    }
}
