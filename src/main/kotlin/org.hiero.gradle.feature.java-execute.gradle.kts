// SPDX-License-Identifier: Apache-2.0
plugins { id("java") }

tasks.withType<JavaExec>().configureEach {
    // Do not yet run things on the '--module-path'
    modularity.inferModulePath = false
    if (name.endsWith("main()")) {
        notCompatibleWithConfigurationCache("JavaExec created by IntelliJ")
    }
}
