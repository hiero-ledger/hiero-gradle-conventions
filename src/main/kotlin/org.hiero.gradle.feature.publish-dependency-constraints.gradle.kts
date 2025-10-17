// SPDX-License-Identifier: Apache-2.0
plugins {
    id("java-library")
    id("org.hiero.gradle.base.jpms-modules")
}

configurations.register("publishDependencyConstraint") {
    extendsFrom(configurations["internal"])
    dependencies.all {
        val constraint = this
        project.dependencies.constraints.add(
            "api",
            incoming.resolutionResult.rootComponent.map {
                (it.dependencies.single {
                        it is ResolvedDependencyResult &&
                            it.selected.moduleVersion?.group == constraint.group &&
                            it.selected.moduleVersion?.name == constraint.name
                    } as ResolvedDependencyResult)
                    .selected
                    .moduleVersion
                    .toString()
            },
        )
    }
}
