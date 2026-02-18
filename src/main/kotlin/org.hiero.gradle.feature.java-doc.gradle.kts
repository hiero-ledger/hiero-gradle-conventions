// SPDX-License-Identifier: Apache-2.0
plugins {
    id("java")
    id("org.gradlex.reproducible-builds")
}

tasks.javadoc {
    // Only enabled if there are Java sources in packages (subdirectories).
    // Needed, as the task fails if there is only a 'module-info.java' as the only source file.
    enabled =
        sourceSets.main.get().java.srcDirs.any {
            it.listFiles()?.any { file -> file.isDirectory } ?: false
        }
    options {
        this as StandardJavadocDocletOptions
        tags(
            "apiNote:a:API Note:",
            "implSpec:a:Implementation Requirements:",
            "implNote:a:Implementation Note:",
        )
        options.windowTitle = "Hiero"
        options.memberLevel = JavadocMemberLevel.PACKAGE
        addStringOption("Xdoclint:all,-missing", "-Xwerror")
    }
}

tasks.assemble { dependsOn(tasks.javadoc) }
