// SPDX-License-Identifier: Apache-2.0
import org.gradlex.javamodule.dependencies.tasks.ModuleDirectivesScopeCheck

plugins {
    id("java")
    id("jacoco-report-aggregation")
    id("org.hiero.gradle.base.jpms-modules")
}

tasks.withType<ModuleDirectivesScopeCheck> { enabled = false }

// Make aggregation "classpath" use the platform for versions (gradle/versions)
configurations.aggregateCodeCoverageReportResults { extendsFrom(configurations["internal"]) }

tasks.check { dependsOn(tasks.testCodeCoverageReport) }
