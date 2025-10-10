// SPDX-License-Identifier: Apache-2.0
package org.hiero.gradle.extensions

import org.gradle.api.IsolatedAction
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create

/**
 * Isolated action to transfer the information about plugin versions from 'settings.gradle.kts' to
 * 'build.gradle.kts' in the projects.
 */
@Suppress("UnstableApiUsage")
class PluginVersionsExtensionCreateAction(val pluginVersions: Map<String, String>) :
    IsolatedAction<Project> {
    override fun execute(project: Project) {
        project.extensions.create("pluginVersions", PluginVersionsExtension::class, pluginVersions)
    }
}
