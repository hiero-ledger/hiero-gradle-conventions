/*
 * Copyright (C) 2024 Hiero a Series of LF Projects, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.hiero.gradle.test

import java.io.File
import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.TaskOutcome.SUCCESS
import org.hiero.gradle.test.fixtures.GradleProject
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

/**
 * This tests makes sure that each plugin can be used individually of the other plugins. When
 * applied in a project context, a plugin sometimes works without declaring the plugins it
 * depends on in its own plugins {} block. This can happen if the required plugins are
 * coincidentally applied before in the project. This test makes sure that each plugin goes
 * through the Gradle configuration phase if it is applied on its own.
 */
class ConventionPluginTest {

    @ParameterizedTest
    @MethodSource("pluginIds")
    fun `each plugin can be applied individually without error`(pluginId: String) {
        val p = GradleProject()
        when {
            pluginId.endsWith(".settings") ->
                p.settingsFile("""plugins { id("${pluginId.substringBeforeLast(".settings")}") }""")
            pluginId.endsWith(".root") ->
                p.file(
                    "build.gradle.kts",
                    """plugins { id("${pluginId.substringBeforeLast(".settings")}") }"""
                )
            else -> p.withMinimalStructure().moduleBuildFile("""plugins { id("$pluginId") }""")
        }

        val result = p.help()

        assertThat(result.task(":help")!!.outcome).isEqualTo(SUCCESS)
    }

    companion object {
        @JvmStatic
        fun pluginIds(): Array<String> {
            val pluginList =
                File("src/main/kotlin")
                    .listFiles()!!
                    .filter { it.isFile && it.name.endsWith(".gradle.kts") }
                    .map { it.name.substringBeforeLast(".gradle.kts") }
            return pluginList.toTypedArray()
        }
    }
}
