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

                    val exports = mutableListOf<List<String>>()
                    val requiresTransitive = mutableListOf<String>()
                    val requires = mutableListOf<String>()
                    val requiresStaticTransitive = mutableListOf<String>()
                    val requiresStatic = mutableListOf<String>()
                    val others = mutableListOf<List<String>>()

                    val current = mutableListOf<String>()

                    fun flushCurrent() {
                        if (current.isEmpty()) return
                        val first = current.first().trim()
                        when {
                            first.startsWith("exports") -> exports.add(current.toList())
                            first.startsWith("requires static transitive") ->
                                requiresStaticTransitive.add(current.first())
                            first.startsWith("requires static") ->
                                requiresStatic.add(current.first())
                            first.startsWith("requires transitive") ->
                                requiresTransitive.add(current.first())
                            first.startsWith("requires") -> requires.add(current.first())
                            else -> others.add(current.toList())
                        }
                        current.clear()
                    }

                    for (line in bodyLines) {
                        if (line.isBlank()) {
                            flushCurrent()
                            continue
                        }
                        current.add(line)
                        if (
                            line.trimEnd().endsWith(";") ||
                                (line.contains(";") &&
                                    line.substringAfter(";").trim().startsWith("//"))
                        ) {
                            flushCurrent()
                        }
                    }
                    flushCurrent()

                    val requiresComparator =
                        Comparator<String> { a, b ->
                            val nameA = a.split(" ").first { it.endsWith(";") }
                            val nameB = b.split(" ").first { it.endsWith(";") }
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
                    exports.sortBy { it.first() }
                    requiresTransitive.sortWith(requiresComparator)
                    requires.sortWith(requiresComparator)
                    requiresStaticTransitive.sortWith(requiresComparator)
                    requiresStatic.sortWith(requiresComparator)

                    val allRequires =
                        requiresTransitive + requires + requiresStaticTransitive + requiresStatic

                    val result = mutableListOf<String>()
                    result.addAll(beforeBody)

                    if (exports.isNotEmpty()) {
                        exports.forEach { result.addAll(it) }
                    }
                    if (exports.isNotEmpty() && allRequires.isNotEmpty()) {
                        result.add("")
                    }
                    if (allRequires.isNotEmpty()) {
                        result.addAll(allRequires)
                    }
                    if ((exports.isNotEmpty() || allRequires.isNotEmpty()) && others.isNotEmpty()) {
                        result.add("")
                    }
                    others.forEach { result.addAll(it) }

                    result.addAll(afterBody)

                    result.joinToString("\n")
                }
            }
        }
    }
}
