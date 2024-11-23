// SPDX-License-Identifier: Apache-2.0
plugins {
    id("java")
    id("org.gradlex.reproducible-builds")
}

tasks.withType<Javadoc>().configureEach {
    options {
        this as StandardJavadocDocletOptions
        tags(
            "apiNote:a:API Note:",
            "implSpec:a:Implementation Requirements:",
            "implNote:a:Implementation Note:"
        )
        options.windowTitle = "Hiero"
        options.memberLevel = JavadocMemberLevel.PACKAGE
        addStringOption("Xdoclint:all,-missing", "-Xwerror")
    }
}

tasks.assemble { dependsOn(tasks.javadoc) }
