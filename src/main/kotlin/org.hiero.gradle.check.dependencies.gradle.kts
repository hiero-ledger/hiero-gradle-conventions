// SPDX-License-Identifier: Apache-2.0
import com.autonomousapps.DependencyAnalysisExtension
import com.autonomousapps.DependencyAnalysisSubExtension
import org.gradlex.javamodule.dependencies.tasks.ModuleDirectivesOrderingCheck
import org.gradlex.javamodule.dependencies.tasks.ModuleDirectivesScopeCheck

plugins {
    id("com.autonomousapps.dependency-analysis")
    id("org.hiero.gradle.base.lifecycle")
    id("org.hiero.gradle.base.jpms-modules")
}

// ordering check is done by SortModuleInfoRequiresStep
tasks.withType<ModuleDirectivesOrderingCheck> { enabled = false }

// Do not report dependencies from one source set to another as 'required'.
// In particular, in case of test fixtures, the analysis would suggest to
// add as testModuleInfo { require(...) } to the main module. This is
// conceptually wrong, because in whitebox testing the 'main' and 'test'
// module are conceptually considered one module (main module extended with tests)
if (project.parent == null) {
    configure<DependencyAnalysisExtension> { issues { all { onAny { exclude(project.path) } } } }
} else {
    configure<DependencyAnalysisSubExtension> { issues { onAny { exclude(project.path) } } }
}

tasks.named("qualityCheck") { dependsOn(tasks.withType<ModuleDirectivesScopeCheck>()) }

tasks.named("qualityGate") { dependsOn(tasks.withType<ModuleDirectivesScopeCheck>()) }
