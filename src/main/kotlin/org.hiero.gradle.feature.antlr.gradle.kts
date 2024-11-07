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

plugins {
    id("java")
    id("antlr")
}

configurations {
    // Treat the ANTLR compiler as a separate tool that should not end up on the compile/runtime
    // classpath of our runtime.
    // https://github.com/gradle/gradle/issues/820
    api { setExtendsFrom(extendsFrom.filterNot { it == antlr.get() }) }
    // Get ANTLR version from 'hedera-dependency-versions'
    antlr { extendsFrom(configurations["internal"]) }
}

dependencies { antlr("org.antlr:antlr4") }

// See: https://github.com/gradle/gradle/issues/25885
tasks.named("sourcesJar") { dependsOn(tasks.generateGrammarSource) }

tasks.withType<com.autonomousapps.tasks.CodeSourceExploderTask>().configureEach {
    dependsOn(tasks.withType<AntlrTask>())
}

tasks.withType<com.diffplug.gradle.spotless.SpotlessTask>().configureEach {
    dependsOn(tasks.withType<AntlrTask>())
}
