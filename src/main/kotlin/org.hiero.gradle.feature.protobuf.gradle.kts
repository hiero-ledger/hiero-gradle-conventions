// SPDX-License-Identifier: Apache-2.0
import com.google.protobuf.gradle.GenerateProtoTask

plugins {
    id("java")
    id("com.google.protobuf")
    id("org.hiero.gradle.base.jpms-modules")
}

// Configure Protobuf Plugin to download protoc executable rather than using local installed version
protobuf {
    protoc { artifact = "com.google.protobuf:protoc:" }
    // Add GRPC plugin as we need to generate GRPC services
    plugins { register("grpc") { artifact = "io.grpc:protoc-gen-grpc-java:" } }
    generateProtoTasks {
        all().configureEach { plugins.register("grpc") { option("@generated=omit") } }
    }
}

configurations.configureEach {
    if (name.startsWith("protobufToolsLocator") || name.endsWith("ProtoPath")) {
        @Suppress("UnstableApiUsage")
        shouldResolveConsistentlyWith(configurations.getByName("mainRuntimeClasspath"))
        attributes { attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.JAVA_API)) }
        exclude(group = project.group.toString(), module = project.name)
        withDependencies {
            isTransitive = true
            extendsFrom(configurations["internal"])
        }
        if (name.startsWith("protobufToolsLocator")) {
            // Track all tools as input to react to version changes for the tools
            val protobufToolsLocator = this
            tasks.withType<GenerateProtoTask>().configureEach { inputs.files(protobufToolsLocator) }
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
