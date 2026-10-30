// SPDX-License-Identifier: Apache-2.0
plugins { id("com.diffplug.spotless") }

spotless {
    format("proto") {
        target("src/main/proto/**/*.proto")
        addStep(org.hiero.gradle.spotless.ProtoLintStep.create())
    }
}
