// SPDX-License-Identifier: Apache-2.0
package org.hiero.gradle.spotless

import com.diffplug.spotless.FormatterFunc
import com.diffplug.spotless.FormatterStep

class SortModuleInfoRequiresStep {
    companion object {
        private const val NAME = "SortModuleInfoRequires"
        private val OWN_PACKAGES = listOf("com.swirlds.", "com.hedera", "org.hiero")

        fun create(): FormatterStep {
            return FormatterStep.create(NAME, State(), State::toFormatter)
        }
    }

    private class State : java.io.Serializable {

        fun toFormatter(): FormatterFunc {
            return FormatterFunc { unixStr ->
                val lines = unixStr.split('\n')

                // Only process module-info.java files (not package-info.java)
                val openBraceIndex = lines.indexOfFirst { it.contains("{") }
                val closeBraceIndex = lines.indexOfLast { it.trim().startsWith("}") }

                if (
                    openBraceIndex == -1 ||
                        closeBraceIndex == -1 ||
                        lines.none { it.trim().startsWith("module ") }
                ) {
                    unixStr
                } else {
                    val beforeBody = lines.subList(0, openBraceIndex + 1)
                    val afterBody = lines.subList(closeBraceIndex, lines.size)
                    val bodyLines = lines.subList(openBraceIndex + 1, closeBraceIndex)

                    val generalExports = mutableListOf<List<String>>()
                    val targetedExports = mutableListOf<List<String>>()
                    val opens = mutableListOf<List<String>>()
                    val requiresTransitive = mutableListOf<List<String>>()
                    val requires = mutableListOf<List<String>>()
                    val requiresStaticTransitive = mutableListOf<List<String>>()
                    val requiresStatic = mutableListOf<List<String>>()
                    val uses = mutableListOf<List<String>>()
                    val provides = mutableListOf<List<String>>()

                    val current = mutableListOf<String>()

                    fun flushCurrent() {
                        if (current.isEmpty()) return
                        when {
                            current.anyLineStartsWith("exports") &&
                                current.any { it.split(" ").contains("to") } ->
                                targetedExports.add(current.toList())
                            current.anyLineStartsWith("exports") ->
                                generalExports.add(current.toList())
                            current.anyLineStartsWith("opens") -> opens.add(current.toList())
                            current.anyLineStartsWith("requires static transitive") ->
                                requiresStaticTransitive.add(current.toList())
                            current.anyLineStartsWith("requires static") ->
                                requiresStatic.add(current.toList())
                            current.anyLineStartsWith("requires transitive") ->
                                requiresTransitive.add(current.toList())
                            current.anyLineStartsWith("requires") -> requires.add(current.toList())
                            current.anyLineStartsWith("uses") -> uses.add(current.toList())
                            current.anyLineStartsWith("provides") -> provides.add(current.toList())
                        }
                        current.clear()
                    }

                    for (line in bodyLines) {
                        if (line.isBlank()) {
                            flushCurrent()
                        } else {
                            current.add(line)
                            if (line.contains(";")) {
                                flushCurrent()
                            }
                        }
                    }
                    flushCurrent()

                    val requiresComparator =
                        Comparator<List<String>> { a, b ->
                            val nameA =
                                a.first { !it.isCommentLine() }
                                    .split(" ")
                                    .first { it.endsWith(";") }
                            val nameB =
                                b.first { !it.isCommentLine() }
                                    .split(" ")
                                    .first { it.endsWith(";") }
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

                    // Sort exports alphabetically by the exported package name
                    generalExports.sortBy { it.first { !it.isCommentLine() } }
                    targetedExports.sortBy { it.first { !it.isCommentLine() } }
                    opens.sortBy { it.first { !it.isCommentLine() } }
                    uses.sortBy { it.first { !it.isCommentLine() } }
                    provides.sortBy { it.first { !it.isCommentLine() } }
                    requiresTransitive.sortWith(requiresComparator)
                    requires.sortWith(requiresComparator)
                    requiresStaticTransitive.sortWith(requiresComparator)
                    requiresStatic.sortWith(requiresComparator)

                    val allRequires =
                        requiresTransitive + requires + requiresStaticTransitive + requiresStatic

                    val result = mutableListOf<String>()
                    result.addAll(beforeBody)

                    result.addBlocks(generalExports)
                    if (
                        targetedExports.isNotEmpty() &&
                            !targetedExports.first().first().isCommentLine()
                    ) {
                        // the generic formatter does not allow a new line here if there is no
                        // comment
                        if (result.last() == "") {
                            result.removeLast()
                        }
                    }
                    result.addBlocks(targetedExports)
                    result.addBlocks(opens)
                    result.addBlocks(allRequires)
                    result.addBlocks(uses)
                    result.addBlocks(provides)

                    if (result.last() == "") {
                        result.removeLast()
                    }

                    result.addAll(afterBody)

                    result.joinToString("\n")
                }
            }
        }

        private fun String.isCommentLine(): Boolean {
            return trim().startsWith("/")
        }

        private fun List<String>.anyLineStartsWith(keyword: String): Boolean {
            return any { it.trim().startsWith(keyword) }
        }

        private fun MutableList<String>.addBlocks(elements: List<List<String>>) {
            if (elements.isNotEmpty() && elements.first().isNotEmpty()) {
                addAll(elements.flatten())
                add("")
            }
        }
    }
}
