// SPDX-License-Identifier: Apache-2.0
plugins { id("java") }

tasks.withType<JavaExec>().configureEach {
    if (name.endsWith("main()")) {
        notCompatibleWithConfigurationCache("JavaExec created by IntelliJ")
    }
}
