// SPDX-License-Identifier: Apache-2.0
plugins {
    id("java")
    id("org.gradlex.java-module-dependencies")
    id("org.hiero.gradle.base.jpms-modules")
}

configurations.implementation {
    withDependencies {
        // If dependencies are not defined explicitly, auto-depend on all projects that are known to
        // contain Java Modules.
        if (isEmpty()) {
            javaModuleDependencies.allLocalModules().forEach { localModule ->
                project.dependencies.add("implementation", project.project(localModule.projectPath))
            }
        }
    }
}

// The 'aggregation' project is not supposed to have source code. The 'java' plugin is applied only
// for its dependency management features. Therefore, the following tasks which are part of
// 'assemble' are disabled.
tasks.compileJava {
    classpath = files()
    enabled = false
}

tasks.processResources { enabled = false }

tasks.jar { enabled = false }
