/*
 * Copyright (C) 2016-2024 Hiero a Series of LF Projects, LLC
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
    id("maven-publish")
    id("org.hiero.gradle.base.lifecycle")
}

tasks.withType<PublishToMavenRepository>().configureEach {
    // Publishing tasks are only enabled if we publish to the matching group.
    // Otherwise, Nexus configuration and credentials do not fit.
    val publishingPackageGroup = providers.gradleProperty("publishingPackageGroup").orNull
    enabled = publishingPackageGroup == project.group
}

publishing.publications.create<MavenPublication>("maven") { from(components["java"]) }

tasks.named("releaseMavenCentral") { dependsOn(tasks.named("publishToSonatype")) }
