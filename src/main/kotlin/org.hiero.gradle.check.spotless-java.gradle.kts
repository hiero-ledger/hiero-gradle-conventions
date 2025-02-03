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
        // enable toggle comment support
        toggleOffOn()
        // apply flavor of google-java-format
        palantirJavaFormat()

        licenseHeader(LicenseHeader.javaFormat(project), "(package|import)")
            .updateYearWithLatest(true)
    }
    format("javaInfoFiles") {
        // separate extension due to https://github.com/diffplug/spotless/issues/532
        target("src/**/module-info.java", "src/**/package-info.java")

        // sort the 'requires' entries in 'module-info' files
        addStep(SortModuleInfoRequiresStep.create())

        licenseHeader(LicenseHeader.javaFormat(project), "(package|import|module|open|@|/\\*\\*)")
            .updateYearWithLatest(true)
    }
}

tasks.named("spotlessJavaInfoFiles") { dependsOn(tasks.named("spotlessJava")) }
