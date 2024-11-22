// SPDX-License-Identifier: Apache-2.0
plugins { id("java") }

tasks.test {
    inputs.property("operatingSystemName", System.getProperty("os.name"))
    inputs.property("operatingSystemArch", System.getProperty("os.arch"))
}
