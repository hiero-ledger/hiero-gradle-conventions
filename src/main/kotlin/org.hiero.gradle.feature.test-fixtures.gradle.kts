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

// Remove implicit dependency from 'testFixtures' to 'main', as we declare all dependencies
// explicitly. This is necessary for the case where a module does not have a 'main' module-info.
// https://github.com/gradle/gradle/blob/d9303339298e6206182fd1f5c7e51f11e4bdff30/subprojects/plugins/src/main/java/org/gradle/api/plugins/JavaTestFixturesPlugin.java#L68
configurations {
    testFixturesApi {
        // Only do this when main does not exist. Otherwise, it breaks compile time visibility in
        // IDEA which does not know about the whitebox testing in the test task that makes 'main'
        // automatically visible in the Gradle build.
        if (sourceSets.main.get().java.srcDirs.all { !it.exists() }) {
            withDependencies { remove(find { it is ProjectDependency && it.name == project.name }) }
        }
    }
}
