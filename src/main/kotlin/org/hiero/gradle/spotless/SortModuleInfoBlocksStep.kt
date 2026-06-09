// SPDX-License-Identifier: Apache-2.0
package org.hiero.gradle.spotless

import com.diffplug.spotless.FormatterFunc
import com.diffplug.spotless.FormatterStep

class SortModuleInfoBlocksStep {
    companion object {
        private const val NAME = "SortModuleInfoBlocks"

        fun create(): FormatterStep = FormatterStep.create(NAME, State(), State::toFormatter)
    }

    private class State : java.io.Serializable {

        fun toFormatter(): FormatterFunc = FormatterFunc { unixStr ->
            val lines = unixStr.split('\n')
            val result = mutableListOf<String>()
            var i = 0

            while (i < lines.size) {
                val line = lines[i]
                if (isMultiLineModuleInfoBlockStart(line)) {
                    result.add(line)
                    i++
                    val blockLines = mutableListOf<String>()
                    while (i < lines.size && !lines[i].trim().startsWith("}")) {
                        if (lines[i].isNotBlank()) {
                            blockLines.add(lines[i])
                        }
                        i++
                    }
                    blockLines.sortBy { it.trim() }
                    result.addAll(blockLines)
                    if (i < lines.size) {
                        result.add(lines[i])
                    }
                } else {
                    result.add(line)
                }
                i++
            }

            result.joinToString("\n")
        }

        private fun isMultiLineModuleInfoBlockStart(line: String) =
            Regex("""^\s*\w+ModuleInfo\s*\{""").containsMatchIn(line) && !line.contains('}')
    }
}
