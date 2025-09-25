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
                val blockStartIndex = lines.indexOfFirst { it.trim().startsWith("requires") }
                val blockEndIndex = lines.indexOfLast { it.trim().startsWith("requires") }

                if (blockStartIndex == -1) {
                    unixStr // not a module-info.java or no 'requires'
                } else {
                    val nonRequiresLines = mutableListOf<String>()

                    val requiresTransitive = mutableListOf<String>()
                    val requires = mutableListOf<String>()
                    val requiresStaticTransitive = mutableListOf<String>()
                    val requiresStatic = mutableListOf<String>()

                    lines.subList(blockStartIndex, blockEndIndex + 1).forEach { line ->
                        when {
                            line.trim().startsWith("requires static transitive") ->
                                requiresStaticTransitive.add(line)
                            line.trim().startsWith("requires static") -> requiresStatic.add(line)
                            line.trim().startsWith("requires transitive") ->
                                requiresTransitive.add(line)
                            line.trim().startsWith("requires") -> requires.add(line)
                            line.isNotBlank() && !line.trim().startsWith("requires") ->
                                nonRequiresLines.add(line)
                        }
                    }

                    val comparator =
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

                    requiresTransitive.sortWith(comparator)
                    requires.sortWith(comparator)
                    requiresStaticTransitive.sortWith(comparator)
                    requiresStatic.sortWith(comparator)

                    val blockStart = lines.subList(0, blockStartIndex)
                    val blockEnd = lines.subList(blockEndIndex + 1, lines.size)

                    (blockStart +
                            nonRequiresLines +
                            requires +
                            requiresTransitive +
                            requiresStaticTransitive +
                            requiresStatic +
                            blockEnd)
                        .joinToString("\n")
                }
            }
        }
    }
}
