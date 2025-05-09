// SPDX-License-Identifier: Apache-2.0
plugins {
    id("java")
    id("org.hiero.gradle.base.jpms-modules")
    // Make sure 'org.gradlex.java-module-dependencies' is applied as the auto-apply through settings
    // may not happen in a non-module project
    id("org.gradlex.java-module-dependencies")
    id("org.gradlex.java-module-testing")
}

extraJavaModuleInfo {
    failOnMissingModuleInfo = false
    failOnAutomaticModules = false
}

javaModuleTesting {
    classpath(testing.suites["test"])
}
