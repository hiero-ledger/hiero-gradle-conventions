// SPDX-License-Identifier: Apache-2.0
import com.github.jengelman.gradle.plugins.shadow.ShadowBasePlugin
import com.github.jengelman.gradle.plugins.shadow.ShadowJavaPlugin
import com.github.jengelman.gradle.plugins.shadow.internal.DefaultDependencyFilter
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins { id("java") }

// Only apply selected plugins and not 'com.gradleup.shadow' to not have
// ShadowApplicationPlugin that would add to the 'assemble' task
plugins.apply(ShadowBasePlugin::class)

plugins.apply(ShadowJavaPlugin::class)

tasks.withType<ShadowJar>().configureEach {
    group = "shadow"
    mergeServiceFiles()

    manifest { attributes("Multi-Release" to "true") }

    // There is an issue in the shadow plugin that it automatically accesses the
    // files in 'runtimeClasspath' while Gradle is building the task graph.
    // See: https://github.com/GradleUp/shadow/issues/882
    dependencyFilter = NoResolveDependencyFilter()
}

class NoResolveDependencyFilter : DefaultDependencyFilter(project) {
    override fun resolve(configuration: FileCollection): FileCollection {
        return configuration
    }
}
