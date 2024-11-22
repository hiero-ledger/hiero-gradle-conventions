// SPDX-License-Identifier: Apache-2.0
plugins { id("java") }

val deactivatedCompileLintOptions =
    listOf(
        // In Gradle, a module does not see the upstream (not-yet-compiled) modules. This could
        // only be solved by calling 'javac' with '--source-module-path' to make other sources
        // known. But this is at odds with how Gradle's incremental compilation calls the
        // compiler for a subset of Java files for each project individually.
        "module", // module not found when doing 'exports to ...'
        "serial", // serializable class ... has no definition of serialVersionUID
        "processing", // No processor claimed any of these annotations: ...
        "try", // auto-closeable resource ignore is never referenced... (AutoClosableLock)
        "missing-explicit-ctor", // class ... declares no explicit constructors

        // Needed because we use deprecation internally and do not fix all uses right away
        "removal",
        "deprecation",

        // The following checks could be activated and fixed:
        "overrides", // overrides equals, but neither it ... overrides hashCode method
        "unchecked",
        "rawtypes"
    )

val deactivatedCompileLintOptionsJava21 =
    listOf(
        "this-escape", // calling public/protected method in constructor
    )

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.add("-implicit:none")
    options.compilerArgs.add("-Werror")
    options.compilerArgs.add("-Xlint:all")
    options.compilerArgs.add("-Xlint:-" + deactivatedCompileLintOptions.joinToString(",-"))
    if (java.targetCompatibility >= JavaVersion.VERSION_21) {
        options.compilerArgs.add(
            "-Xlint:-" + deactivatedCompileLintOptionsJava21.joinToString(",-")
        )
    }
}
