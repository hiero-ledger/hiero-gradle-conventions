/*
 * Copyright (C) 2022-2024 Hiero a Series of LF Projects, LLC
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

plugins { id("com.diffplug.spotless") }

spotless {
    format("actionYaml") {
        target(".github/workflows/*.yaml")
        /*
         * Prettier requires NodeJS and NPM installed; however, the NodeJS Gradle plugin and Spotless do not yet
         * integrate with each other. Currently there is an open issue report against spotless.
         *
         *   *** Please see for more information: https://github.com/diffplug/spotless/issues/728 ***
         *
         * The workaround provided in the above issue does not work in Gradle 7.5+ and therefore is not a viable solution.
         */
        // prettier()

        trimTrailingWhitespace()
        indentWithSpaces()
        endWithNewline()

        licenseHeader(
                """
            ##
            # Copyright (C) ${'$'}YEAR Hedera Hashgraph, LLC
            #
            # Licensed under the Apache License, Version 2.0 (the "License");
            # you may not use this file except in compliance with the License.
            # You may obtain a copy of the License at
            #
            #      http://www.apache.org/licenses/LICENSE-2.0
            #
            # Unless required by applicable law or agreed to in writing, software
            # distributed under the License is distributed on an "AS IS" BASIS,
            # WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
            # See the License for the specific language governing permissions and
            # limitations under the License.
            ##${"\n\n"}
        """
                    .trimIndent(),
                "(name)"
            )
            .updateYearWithLatest(true)
    }
}
