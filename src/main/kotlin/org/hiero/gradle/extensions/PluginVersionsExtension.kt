// SPDX-License-Identifier: Apache-2.0
package org.hiero.gradle.extensions

import javax.inject.Inject

/** Extension to access versions of plugins that are defined in the 'settings.gradle.kts' file. */
abstract class PluginVersionsExtension
@Inject
constructor(private val versions: Map<String, String>) {

    /** Get the version of the plugin with the given ID. */
    fun version(id: String): String =
        versions[id] ?: throw RuntimeException("Plugin not defined in settings.gradle.kts: $id")
}
