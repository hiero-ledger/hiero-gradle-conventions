// SPDX-License-Identifier: Apache-2.0
import org.gradle.api.component.AdhocComponentWithVariants

plugins {
    id("java")
    id("java-test-fixtures")
}

// Disable publishing of test fixture if 'java-test-fixtures' plugin is used
// https://docs.gradle.org/current/userguide/java_testing.html#ex-disable-publishing-of-test-fixtures-variants
(components["java"] as AdhocComponentWithVariants).apply {
    withVariantsFromConfiguration(configurations["testFixturesApiElements"]) { skip() }
    withVariantsFromConfiguration(configurations["testFixturesRuntimeElements"]) { skip() }
}
