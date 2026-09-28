// SPDX-License-Identifier: Apache-2.0
package org.hiero.gradle.spotless

import com.diffplug.spotless.FormatterFunc
import com.diffplug.spotless.FormatterStep

class SortModuleInfoBlocksStep {
    companion object {
        private const val NAME = "SortModuleInfoBlocks"
        private val OWN_PACKAGES = listOf("com.swirlds.", "com.hedera", "org.hiero")

        fun create(): FormatterStep = FormatterStep.create(NAME, State(), State::toFormatter)
    }

    private class State : java.io.Serializable {

        fun toFormatter(): FormatterFunc = FormatterFunc { unixStr ->
            val lines = unixStr.split('\n')
            val result = mutableListOf<String>()
            var i = 0

            val ownPackagesComparator =
                Comparator<String> { a, b ->
                    val nameA = a.trim().substringAfter("(\"").substringBefore("\")")
                    val nameB = b.trim().substringAfter("(\"").substringBefore("\")")
                    if (
                        OWN_PACKAGES.any { nameA.startsWith(it) } &&
                            OWN_PACKAGES.none { nameB.startsWith(it) }
                    ) {
                        -1
                    } else if (
                        OWN_PACKAGES.none { nameA.startsWith(it) } &&
                            OWN_PACKAGES.any { nameB.startsWith(it) }
                    ) {
                        1
                    } else {
                        nameA.compareTo(nameB)
                    }
                }

            while (i < lines.size) {
                val line = lines[i]
                if (line.isMultiLineModuleInfoBlockStart()) {
                    result.add(line)
                    i++

                    val annotationProcessors = mutableListOf<String>()
                    val requires = mutableListOf<String>()
                    val requiresStatic = mutableListOf<String>()
                    val runtimeOnly = mutableListOf<String>()
                    val others = mutableListOf<String>()

                    while (i < lines.size && !lines[i].trim().startsWith("}")) {
                        val entry = lines[i]
                        if (entry.isNotBlank()) {
                            val trimmed = entry.trim()
                            when {
                                trimmed.startsWith("annotationProcessor(") ->
                                    annotationProcessors.add(entry)
                                trimmed.startsWith("requiresStatic(") -> requiresStatic.add(entry)
                                trimmed.startsWith("requires(") -> requires.add(entry)
                                trimmed.startsWith("runtimeOnly(") -> runtimeOnly.add(entry)
                                else -> others.add(entry) // exportsTo, opensTo
                            }
                        }
                        i++
                    }

                    annotationProcessors.sortWith(ownPackagesComparator)
                    requires.sortWith(ownPackagesComparator)
                    requiresStatic.sortWith(ownPackagesComparator)
                    runtimeOnly.sortWith(ownPackagesComparator)
                    others.sortBy { it.trim() }

                    val groups =
                        listOf(annotationProcessors, requires + requiresStatic, runtimeOnly, others)
                            .filter { it.isNotEmpty() }

                    groups.forEachIndexed { idx, group ->
                        if (idx > 0) result.add("")
                        result.addAll(group)
                    }

                    if (i < lines.size) {
                        result.add(lines[i]) // closing brace
                    }
                } else {
                    result.add(line)
                }
                i++
            }

            result.joinToString("\n")
        }

        private fun String.isMultiLineModuleInfoBlockStart() =
            Regex("""^\s*\w+ModuleInfo\s*\{""").containsMatchIn(this) && !this.contains('}')
    }
}
