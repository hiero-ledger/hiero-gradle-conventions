/*
 * Copyright (C) 2023-2024 Hiero a Series of LF Projects, LLC
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
    id("com.google.protobuf")
}

// Configure Protobuf Plugin to download protoc executable rather than using local installed version
protobuf {
    protoc { artifact = "com.google.protobuf:protoc" }
    // Add GRPC plugin as we need to generate GRPC services
    plugins { register("grpc") { artifact = "io.grpc:protoc-gen-grpc-java" } }
    generateProtoTasks {
        all().configureEach { plugins.register("grpc") { option("@generated=omit") } }
    }
}

configurations.configureEach {
    if (name.startsWith("protobufToolsLocator") || name.endsWith("ProtoPath")) {
        attributes { attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.JAVA_API)) }
        exclude(group = project.group.toString(), module = project.name)
        withDependencies {
            isTransitive = true
            extendsFrom(configurations["internal"])
        }
    }
}

tasks.javadoc {
    options {
        this as StandardJavadocDocletOptions
        // There are violations in the generated protobuf code
        addStringOption("Xdoclint:-reference,-html", "-quiet")
    }
}
