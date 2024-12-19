// SPDX-License-Identifier: Apache-2.0
import org.hiero.gradle.spotless.LicenseHeader
import org.hiero.gradle.spotless.RepairDashedCommentsFormatterStep
import org.hiero.gradle.spotless.SortModuleInfoRequiresStep

plugins { id("com.diffplug.spotless") }

spotless {
    java {
        targetExclude("build/generated/sources/**/*.java", "build/generated/source/**/*.java")

        // fix errors due to dashed comment blocks (eg: /*-, /*--, etc)
        addStep(RepairDashedCommentsFormatterStep.create())
        // Sort the 'requires' entries in Module Info files
        addStep(SortModuleInfoRequiresStep.create())
        // enable toggle comment support
        toggleOffOn()
        // don't need to set target, it is inferred from java
        // apply a specific flavor of google-java-format
        palantirJavaFormat()
        // make sure every file has the following copyright header.
        // optionally, Spotless can set copyright years by digging
        // through git history (see "license" section below).
        // The delimiter override below is required to support some
        // of our test classes which are in the default package.
        licenseHeader(LicenseHeader.javaFormat(project), "(package|import|module)")
            .updateYearWithLatest(true)
    }
}
