// SPDX-License-Identifier: Apache-2.0
package org.hiero.gradle.spotless

object LicenseHeader {
    private const val SPDX_IDENTIFIER = "SPDX-License-Identifier: Apache-2.0"
    const val HEADER_STYLE_C = "// $SPDX_IDENTIFIER\n"
    const val HEADER_STYLE_SHELL = "# $SPDX_IDENTIFIER\n"
    const val FIRST_LINE_REGEX_STYLE_C = "^(?!\\/\\/ SPDX)."
    const val FIRST_LINE_REGEX_STYLE_SHELL = "^(?!# SPDX)."
}
