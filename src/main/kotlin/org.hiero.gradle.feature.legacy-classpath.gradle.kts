// SPDX-License-Identifier: Apache-2.0
plugins { id("org.hiero.gradle.base.jpms-modules") }

// Make sure 'org.gradlex.java-module-dependencies' is applied as the auto-apply through settings
// may not happen in a non-module project
apply(plugin = "org.gradlex.java-module-dependencies")

extraJavaModuleInfo {
    failOnMissingModuleInfo = false
    failOnAutomaticModules = false
}
