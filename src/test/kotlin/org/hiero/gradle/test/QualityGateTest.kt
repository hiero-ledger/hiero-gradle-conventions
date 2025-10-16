// SPDX-License-Identifier: Apache-2.0
package org.hiero.gradle.test

import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.TaskOutcome
import org.hiero.gradle.test.fixtures.GradleProject
import org.junit.jupiter.api.Test

class QualityGateTest {

    @Test
    fun `qualityGate formats yml and yaml files`() {
        val p = GradleProject().withMinimalStructure()
        val flow1 = p.file(".github/workflows/flow1.yml", "name: Flow 1    ")
        val flow2 = p.file(".github/workflows/flow2.yaml", "name:      Flow 2    ")
        val bot = p.file(".github/dependabot.yml", "updates:    ")
        val txtFile = p.file(".github/workflows/temp.txt", "name: Flow 3    ")

        val result = p.qualityGate()

        assertThat(flow1)
            .hasContent(
                """
            # SPDX-License-Identifier: Apache-2.0
            name: Flow 1
        """
                    .trimIndent()
            )
        assertThat(flow2)
            .hasContent(
                """
            # SPDX-License-Identifier: Apache-2.0
            name: Flow 2
        """
                    .trimIndent()
            )
        assertThat(bot)
            .hasContent(
                """
            # SPDX-License-Identifier: Apache-2.0
            updates:
        """
                    .trimIndent()
            )
        assertThat(txtFile).hasContent("name: Flow 3    ") // unchanged

        assertThat(result.task(":qualityGate")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }

    @Test
    fun `qualityGate fully formats module-info and package-info`() {
        val p = GradleProject().withMinimalStructure()
        p.moduleBuildFile("""plugins { id("org.hiero.gradle.module.library") }""")
        p.dependencyVersionsFile(
            """
            plugins {
                id("org.hiero.gradle.base.lifecycle")
                id("org.hiero.gradle.base.jpms-modules")
            }
            dependencies.constraints {
                api("com.fasterxml.jackson.core:jackson-databind:2.16.0") { because("com.fasterxml.jackson.databind") }
                api("org.apache.commons:commons-lang3:3.14.0") { because("org.apache.commons.lang3") }
            }"""
                .trimIndent()
        )
        p.javaSourceFile(
            """
            package org.hiero.product.module.a;
            public class ModuleA {
                private com.fasterxml.jackson.databind.ObjectMapper om;
                private org.apache.commons.lang3.CharUtils cu;
            }"""
                .trimIndent()
        )

        val moduleInfo =
            p.file(
                "product/module-a/src/main/java/module-info.java",
                """
                module org.hiero.product.module.a   {    
                    requires org.apache.commons.lang3;  
                    requires    com.fasterxml.jackson.databind;
                    
                    exports       org.hiero.product.module.a;
                  }    """
                    .trimIndent(),
            )
        val packageInfoA =
            p.file(
                "product/module-a/src/main/java/org/hiero/product/module/a/package-info.java",
                "package     org.hiero.product.module.a;  ",
            )
        val packageInfoB =
            p.file(
                "product/module-a/src/main/java/org/hiero/product/module/b/package-info.java",
                "/** some comment */    package     org.hiero.product.module.b;  ",
            )
        val packageInfoC =
            p.file(
                "product/module-a/src/main/java/org/hiero/product/module/c/package-info.java",
                "@Deprecated   package     org.hiero.product.module.c;  ",
            )

        val result = p.qualityGate()

        assertThat(moduleInfo)
            .hasContent(
                """
            // SPDX-License-Identifier: Apache-2.0
            module org.hiero.product.module.a {
                requires com.fasterxml.jackson.databind;
                requires org.apache.commons.lang3;
            
                exports org.hiero.product.module.a;
            }
        """
                    .trimIndent()
            )
        assertThat(packageInfoA)
            .hasContent(
                """
            // SPDX-License-Identifier: Apache-2.0
            package org.hiero.product.module.a;
        """
                    .trimIndent()
            )
        assertThat(packageInfoB)
            .hasContent(
                """
            // SPDX-License-Identifier: Apache-2.0
            /** some comment */
            package org.hiero.product.module.b;
        """
                    .trimIndent()
            )
        assertThat(packageInfoC)
            .hasContent(
                """
            // SPDX-License-Identifier: Apache-2.0
            @Deprecated
            package org.hiero.product.module.c;
        """
                    .trimIndent()
            )

        assertThat(result.task(":qualityGate")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }

    @Test
    fun `qualityGate formats property files`() {
        val p = GradleProject().withMinimalStructure()
        val props1 = p.file("props1.properties", "\nfoo=bar    ")

        val result = p.qualityGate()

        assertThat(props1)
            .hasContent(
                """
            # SPDX-License-Identifier: Apache-2.0
            
            foo=bar
        """
                    .trimIndent()
            )

        assertThat(result.task(":qualityGate")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }

    @Test
    fun `spotlessApply formats rust files`() {
        val p = GradleProject().withMinimalStructure()
        p.moduleBuildFile(
            """plugins {
            id("org.hiero.gradle.module.library")
            id("org.hiero.gradle.feature.rust") 
            }"""
                .trimIndent()
        )

        val rustLib = p.file("product/module-a/src/main/rust/lib.rs", "pub fn public_api() {}")

        val result = p.run("spotlessApply")

        assertThat(rustLib)
            .hasContent(
                """
            // SPDX-License-Identifier: Apache-2.0
            
            pub fn public_api() {}
        """
                    .trimIndent()
            )

        assertThat(result.task(":spotlessApply")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }
}
