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

    // https://gradleup.com/shadow/changes/#migration-example
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
    mergeServiceFiles()

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
