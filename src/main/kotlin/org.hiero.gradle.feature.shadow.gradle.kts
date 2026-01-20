// SPDX-License-Identifier: Apache-2.0
import com.github.jengelman.gradle.plugins.shadow.ShadowBasePlugin
import com.github.jengelman.gradle.plugins.shadow.ShadowJavaPlugin
import com.github.jengelman.gradle.plugins.shadow.tasks.DependencyFilter
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins { id("java") }

// Only apply selected plugins and not 'com.gradleup.shadow' to not have
// ShadowApplicationPlugin that would add to the 'assemble' task
plugins.apply(ShadowBasePlugin::class)

plugins.apply(ShadowJavaPlugin::class)

tasks.withType<ShadowJar>().configureEach {
    group = "shadow"

    // allow shadow Jar files to have more than 64k entries
    isZip64 = true

    duplicatesStrategy = DuplicatesStrategy.FAIL

    // For entries for which duplications is expected, add only the first (and EXCLUDE others)
    filesMatching("*") { duplicatesStrategy = DuplicatesStrategy.EXCLUDE }
    filesMatching("META-INF/*") { duplicatesStrategy = DuplicatesStrategy.EXCLUDE }
    filesMatching("META-INF/helidon/*") { duplicatesStrategy = DuplicatesStrategy.EXCLUDE }

    // For service registrations include dupllicates as they are merged into one
    // https://gradleup.com/shadow/changes/#migration-example
    filesMatching("META-INF/services/**") { duplicatesStrategy = DuplicatesStrategy.INCLUDE }
    mergeServiceFiles()

    // Standard excludes (same as tasks.shadowJar has by default)
    exclude(
        "META-INF/INDEX.LIST",
        "META-INF/*.SF",
        "META-INF/*.DSA",
        "META-INF/*.RSA",
        "META-INF/versions/**/module-info.class",
        "module-info.class",
        "META-INF/versions/**/OSGI-INF/MANIFEST.MF",
        "META-INF/maven/**",
    )

    manifest { attributes("Multi-Release" to "true") }

    // There is an issue in the shadow plugin that it automatically accesses the
    // files in 'runtimeClasspath' while Gradle is building the task graph.
    // See: https://github.com/GradleUp/shadow/issues/882
    dependencyFilter = NoResolveDependencyFilter(project)
}

class NoResolveDependencyFilter(p: Project) : DependencyFilter.AbstractDependencyFilter(p) {

    override fun resolve(configuration: Configuration): FileCollection {
        // override, to not do early dependency resolution
        return configuration
    }

    override fun resolve(
        dependencies: Set<ResolvedDependency>,
        includedDependencies: MutableSet<ResolvedDependency>,
        excludedDependencies: MutableSet<ResolvedDependency>,
    ) {
        // implementation is copied from DefaultDependencyFilter
        dependencies.forEach {
            if (
                if (it.isIncluded()) includedDependencies.add(it) else excludedDependencies.add(it)
            ) {
                resolve(it.children, includedDependencies, excludedDependencies)
            }
        }
    }
}
