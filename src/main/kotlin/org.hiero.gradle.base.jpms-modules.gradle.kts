// SPDX-License-Identifier: Apache-2.0
plugins {
    id("org.gradlex.jvm-dependency-conflict-resolution")
    id("org.gradlex.extra-java-module-info")
}

// Fix or enhance the metadata of third-party Modules. This is about the metadata in the
// repositories: '*.pom' and '*.module' files.
jvmDependencyConflicts.patch {
    // Register JARs with classifier as features
    module("io.netty:netty-transport-native-epoll") {
        addFeature("linux-x86_64") // refer to as 'io.netty.transport.epoll.linux.x86_64'
        addFeature("linux-aarch_64") // refer to as 'io.netty.transport.epoll.linux.aarch_64'
    }

    // Remove transitive dependencies that are not used
    module("io.prometheus:simpleclient") {
        removeDependency("io.prometheus:simpleclient_tracer_otel") // not needed
        removeDependency("io.prometheus:simpleclient_tracer_otel_agent") // not needed
    }
    module("junit:junit") {
        removeDependency("org.hamcrest:hamcrest-core") // not needed
    }
    module("org.jetbrains.kotlin:kotlin-stdlib") {
        removeDependency("org.jetbrains.kotlin:kotlin-stdlib-common") // not needed
    }
    module("biz.aQute.bnd:biz.aQute.bnd.annotation") {
        removeDependency("org.osgi:org.osgi.resource") // split package
        removeDependency("org.osgi:org.osgi.service.serviceloader") // split package
    }

    // Add missing compile time dependencies
    module("org.hyperledger.besu:secp256k1") {
        addApiDependency("net.java.dev.jna:jna") // access annotation at compile time
    }
    module("uk.org.webcompere:system-stubs-jupiter") {
        addApiDependency("org.junit.jupiter:junit-jupiter-api") // needed for super class
    }

    // Add missing runtime dependencies
    module("org.rnorth.duct-tape:duct-tape") {
        addRuntimeOnlyDependency("org.slf4j:slf4j-api") // wrongly marked as provided
    }

    // Reduce scope of transitively added annotation libraries
    val annotationLibrariesCompileTime =
        listOf("com.google.code.findbugs:jsr305", "org.jspecify:jspecify")
    val annotationLibrariesUnused =
        listOf(
            "com.google.android:annotations",
            "org.checkerframework:checker-compat-qual",
            "org.codehaus.mojo:animal-sniffer-annotations",
        )
    val modulesUsingAnnotationLibraries =
        listOf(
            "com.github.ben-manes.caffeine:caffeine",
            "com.google.dagger:dagger-compiler",
            "com.google.dagger:dagger-producers",
            "com.google.dagger:dagger-spi",
            "com.google.guava:guava",
            "com.google.protobuf:protobuf-java-util",
            "io.grpc:grpc-api",
            "io.grpc:grpc-context",
            "io.grpc:grpc-core",
            "io.grpc:grpc-netty",
            "io.grpc:grpc-netty-shaded",
            "io.grpc:grpc-protobuf",
            "io.grpc:grpc-protobuf-lite",
            "io.grpc:grpc-services",
            "io.grpc:grpc-stub",
            "io.grpc:grpc-testing",
            "io.grpc:grpc-util",
        )
    modulesUsingAnnotationLibraries.forEach { module ->
        module(module) {
            annotationLibrariesCompileTime.forEach { reduceToCompileOnlyApiDependency(it) }
            annotationLibrariesUnused.forEach { removeDependency(it) }
        }
    }
}

// Fix or enhance the 'module-info.class' of third-party Modules. This is about the
// 'module-info.class' inside the Jar files. In our full Java Modules setup every
// Jar needs to have this file. If it is missing, it is added by what is configured here.
extraJavaModuleInfo {
    failOnAutomaticModules = true // Only allow Jars with 'module-info' on all module paths
    versionsProvidingConfiguration = "mainRuntimeClasspath"

    module("io.grpc:grpc-api", "io.grpc") {
        exportAllPackages()
        requireAllDefinedDependencies()
        requires("java.logging")
        uses("io.grpc.LoadBalancerProvider")
        uses("io.grpc.ManagedChannelProvider")
        uses("io.grpc.NameResolverProvider")
        uses("io.grpc.ServerProvider")
    }
    module("io.grpc:grpc-core", "io.grpc.internal") {
        exportAllPackages()
        requireAllDefinedDependencies()
        requires("java.logging")
    }
    module("io.grpc:grpc-context", "io.grpc.context")
    module("io.grpc:grpc-inprocess", "io.grpc.inprocess") {
        exportAllPackages()
        requireAllDefinedDependencies()
        requires("java.logging")
    }
    module("io.grpc:grpc-netty", "io.grpc.netty") {
        exportAllPackages()
        requireAllDefinedDependencies()
        requires("java.logging")
        requires("io.netty.buffer")
        requires("io.netty.codec")
        requires("io.netty.codec.http")
        requires("io.netty.common")
        requires("io.netty.handler")
        requires("io.netty.transport")
    }
    module("io.grpc:grpc-stub", "io.grpc.stub") {
        exportAllPackages()
        requireAllDefinedDependencies()
        requires("java.logging")
    }
    module("io.grpc:grpc-util", "io.grpc.util")
    module("io.grpc:grpc-protobuf", "io.grpc.protobuf")
    module("io.grpc:grpc-protobuf-lite", "io.grpc.protobuf.lite")
    module("biz.aQute.bnd:biz.aQute.bnd.annotation", "biz.aQute.bnd.annotation") {
        exportAllPackages()
        // https://github.com/gradlex-org/extra-java-module-info/issues/186
    }
    module(
        "com.carrotsearch.thirdparty:simple-xml-safe",
        "com.carrotsearch.thirdparty.simple.xml.safe",
    ) {
        exportAllPackages()
        requireAllDefinedDependencies()
        requires("java.xml")
    }
    module("com.github.spotbugs:spotbugs-annotations", "com.github.spotbugs.annotations")
    module("com.google.code.findbugs:jsr305", "java.annotation")
    module("com.google.protobuf:protobuf-javalite", "com.google.protobuf") {
        exportAllPackages()
        requireAllDefinedDependencies()
        requires("java.logging")
        requires("jdk.unsupported")
    }
    module("com.google.protobuf:protobuf-java", "com.google.protobuf") {
        exportAllPackages()
        requireAllDefinedDependencies()
        requires("java.logging")
        requires("jdk.unsupported")
    }
    module("com.google.api.grpc:proto-google-common-protos", "com.google.api.grpc.common")
    module("com.google.dagger:dagger", "dagger")
    module("com.squareup:kotlinpoet-jvm", "com.squareup.kotlinpoet")
    module("com.squareup:kotlinpoet", "com.squareup.kotlinpoet")
    module("com.squareup.okhttp3:okhttp", "okhttp3") {
        exportAllPackages()
        requireAllDefinedDependencies()
        requires("java.logging")
    }
    module("com.squareup.okio:okio-jvm", "okio") {
        exportAllPackages()
        requireAllDefinedDependencies()
        requires("java.logging")
    }
    module("com.squareup.okio:okio", "okio")
    module("io.perfmark:perfmark-api", "io.perfmark")
    module("javax.inject:javax.inject", "javax.inject")
    module("com.esaulpaugh:headlong", "com.esaulpaugh.headlong") {
        exportAllPackages()
        requireAllDefinedDependencies()
    }
    module("org.connid:framework", "org.connid.framework")
    module("org.connid:framework-internal", "org.connid.framework.internal") {
        exportAllPackages()
        requires("org.connid.framework") // this is missing in POM
    }
    module("io.tmio:tuweni-units", "tuweni.units")
    module("io.tmio:tuweni-bytes", "tuweni.bytes")
    module("net.i2p.crypto:eddsa", "net.i2p.crypto.eddsa")
    module("io.minio:minio", "io.minio") {
        exportAllPackages()
        requireAllDefinedDependencies()
        requires("java.logging")
        requiresStatic("com.github.spotbugs.annotations")
    }
    module("org.antlr:antlr4-runtime", "org.antlr.antlr4.runtime")
    module("org.hyperledger.besu.internal:algorithms", "org.hyperledger.besu.internal.crypto")
    module("org.hyperledger.besu.internal:rlp", "org.hyperledger.besu.internal.rlp")
    module("org.hyperledger.besu:arithmetic", "org.hyperledger.besu.nativelib.arithmetic")
    module("org.hyperledger.besu:blake2bf", "org.hyperledger.besu.nativelib.blake2bf")
    module("org.hyperledger.besu:bls12-381", "org.hyperledger.besu.nativelib.bls12_381")
    module("org.hyperledger.besu:besu-datatypes", "org.hyperledger.besu.datatypes")
    module("org.hyperledger.besu:besu-native-common", "org.hyperledger.besu.nativelib.common")
    module("org.hyperledger.besu:evm", "org.hyperledger.besu.evm") {
        exportAllPackages()
        requireAllDefinedDependencies()
        requiresStatic("com.fasterxml.jackson.annotation")
    }
    module("org.hyperledger.besu:secp256k1", "org.hyperledger.besu.nativelib.secp256k1")
    module("org.hyperledger.besu:secp256r1", "org.hyperledger.besu.nativelib.secp256r1")
    module("org.hyperledger.besu:gnark", "org.hyperledger.besu.nativelib.gnark")
    module("com.goterl:resource-loader", "com.goterl.resourceloader")
    module("com.goterl:lazysodium-java", "com.goterl.lazysodium")
    // 'io.consensys.protocols' replaces 'tech.pegasys' in org.hyperledger.besu:evm:25.x
    // once 24.x is no longer used, 'tech.pegasys' rule can be removed.
    module("tech.pegasys:jc-kzg-4844", "tech.pegasys.jckzg4844")
    module("io.consensys.protocols:jc-kzg-4844", "io.consensys.protocols.jckzg4844")
    module("net.java.dev.jna:jna", "com.sun.jna") {
        exportAllPackages()
        requires("java.logging")
    }
    module("org.xerial.snappy:snappy-java", "org.xerial.snappy.java")
    module("io.prometheus:prometheus-metrics-config", "io.prometheus.metrics.config")
    module("io.prometheus:prometheus-metrics-core", "io.prometheus.metrics.core") {
        exportAllPackages()
        requires("io.prometheus.metrics.config")
        requires("io.prometheus.metrics.model")
        // io.prometheus:prometheus-metrics-tracer-initializer is excluded
    }
    module(
        "io.prometheus:prometheus-metrics-exposition-formats",
        "io.prometheus.metrics.expositionformats",
    )
    module(
        "io.prometheus:prometheus-metrics-exposition-formats-no-protobuf",
        "io.prometheus.metrics.expositionformats.noprotobuf",
    )
    module("io.prometheus:prometheus-metrics-exposition-textformats", "io.prometheus.writer.text")
    module("io.prometheus:prometheus-metrics-model", "io.prometheus.metrics.model")
    module(
        "io.prometheus:prometheus-metrics-shaded-protobuf",
        "io.prometheus.metrics.shaded.protobuf",
    )
    module("io.prometheus:prometheus-metrics-tracer-common", "io.prometheus.metrics.tracer.common")
    module(
        "io.prometheus:prometheus-metrics-tracer-initializer",
        "io.prometheus.metrics.tracer.initializer",
    )
    module("io.prometheus:prometheus-metrics-tracer-otel", "io.prometheus.metrics.tracer.otel")
    module(
        "io.prometheus:prometheus-metrics-tracer-otel-agent",
        "io.prometheus.metrics.tracer.otel_agent",
    )
    module("io.prometheus:simpleclient", "simpleclient")
    module("io.prometheus:simpleclient_common", "simpleclient.common")
    module("io.prometheus:simpleclient_httpserver", "simpleclient.httpserver") {
        exportAllPackages()
        requireAllDefinedDependencies()
        requires("jdk.httpserver")
    }
    module("io.prometheus:simpleclient_tracer_common", "simpleclient.tracer.common")
    module("io.micrometer:micrometer-commons", "micrometer.commons")
    module("io.micrometer:micrometer-core", "micrometer.core")
    module("io.micrometer:micrometer-observation", "micrometer.observation") {
        exportAllPackages()
        requireAllDefinedDependencies()
        // This is optional from io.micrometer:context-propagation and we do not use it
        ignoreServiceProvider("io.micrometer.context.ThreadLocalAccessor")
    }
    module("io.micrometer:micrometer-registry-prometheus", "micrometer.registry.prometheus")
    module(
        "io.micrometer:micrometer-registry-prometheus-simpleclient",
        "micrometer.registry.prometheus.simpleclient",
    )
    module("org.hdrhistogram:HdrHistogram", "org.hdrhistogram")
    module("org.latencyutils:LatencyUtils", "org.latencyutils")
    module("org.osgi:org.osgi.annotation.bundle", "org.osgi.annotation.bundle") {
        exportAllPackages()
        // https://github.com/gradlex-org/extra-java-module-info/issues/186
        requires("org.osgi.annotation.versioning")
    }
    module("org.osgi:org.osgi.annotation.versioning", "org.osgi.annotation.versioning") {
        exportAllPackages()
        // https://github.com/gradlex-org/extra-java-module-info/issues/186
    }

    // Annotation processing only
    module("com.google.auto.service:auto-service-annotations", "com.google.auto.service")
    module("com.google.auto.service:auto-service", "com.google.auto.service.processor")
    module("com.google.auto:auto-common", "com.google.auto.common")
    module("com.google.dagger:dagger-compiler", "dagger.compiler")
    module("com.google.dagger:dagger-producers", "dagger.producers")
    module("com.google.dagger:dagger-spi", "dagger.spi")
    module(
        "com.google.devtools.ksp:symbol-processing-api",
        "com.google.devtools.ksp.symbolprocessingapi",
    )
    module("com.google.errorprone:javac-shaded", "com.google.errorprone.javac.shaded")
    module("com.google.googlejavaformat:google-java-format", "com.google.googlejavaformat")
    module("net.ltgt.gradle.incap:incap", "net.ltgt.gradle.incap")
    module("org.jetbrains.kotlinx:kotlinx-metadata-jvm", "kotlinx.metadata.jvm")

    // Testing only
    module("io.grpc:grpc-netty-shaded", "io.grpc.netty.shaded") {
        exportAllPackages()
        requireAllDefinedDependencies()
        requires("java.logging")
        requires("jdk.unsupported")
        ignoreServiceProvider("reactor.blockhound.integration.BlockHoundIntegration")
    }
    module("com.google.jimfs:jimfs", "com.google.common.jimfs") {
        exportAllPackages()
        requireAllDefinedDependencies()
        requires("java.logging")
    }
    module("io.github.json-snapshot:json-snapshot", "json.snapshot")
    module("org.awaitility:awaitility", "awaitility")
    module("uk.org.webcompere:system-stubs-core", "uk.org.webcompere.systemstubs.core") {
        exportAllPackages()
        requireAllDefinedDependencies()
        requires("java.instrument")
    }
    module("uk.org.webcompere:system-stubs-jupiter", "uk.org.webcompere.systemstubs.jupiter")

    // Testing only
    module("com.github.docker-java:docker-java-api", "com.github.dockerjava.api")
    module("com.github.docker-java:docker-java-transport", "com.github.dockerjava.transport")
    module(
        "com.github.docker-java:docker-java-transport-zerodep",
        "com.github.dockerjava.transport.zerodep",
    )
    module("com.google.protobuf:protobuf-java-util", "com.google.protobuf.util") {
        exportAllPackages()
        requireAllDefinedDependencies()
        requires("java.logging")
    }
    module("com.squareup:javapoet", "com.squareup.javapoet") {
        exportAllPackages()
        requires("java.compiler")
    }
    module("junit:junit", "junit")
    module("org.hamcrest:hamcrest", "org.hamcrest")
    module("org.objenesis:objenesis", "org.objenesis")
    module("org.rnorth.duct-tape:duct-tape", "org.rnorth.ducttape")
    module("org.testcontainers:testcontainers", "org.testcontainers") {
        exportAllPackages()
        requireAllDefinedDependencies()
        requires("java.management")
        requires("java.sql")
        uses("org.testcontainers.core.CreateContainerCmdModifier")
        uses("org.testcontainers.dockerclient.DockerClientProviderStrategy")
        uses("org.testcontainers.utility.ImageNameSubstitutor")
    }
    module("org.testcontainers:junit-jupiter", "org.testcontainers.junit.jupiter")
}

// Configure consistent resolution across the whole project
val consistentResolutionAttribute = Attribute.of("consistent-resolution", String::class.java)

configurations.create("allDependencies") {
    isCanBeConsumed = true
    isCanBeResolved = false
    sourceSets.all {
        extendsFrom(
            configurations[this.implementationConfigurationName],
            configurations[this.compileOnlyConfigurationName],
            configurations[this.runtimeOnlyConfigurationName],
            configurations[this.annotationProcessorConfigurationName],
        )
    }
    attributes {
        attribute(consistentResolutionAttribute, "global")
        attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.JAVA_RUNTIME))
        attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.LIBRARY))
        attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements.JAR))
        attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling.EXTERNAL))
    }
}

jvmDependencyConflicts.consistentResolution {
    if (project.path == ":") {
        // single project build, e.g. for examples
        providesVersions(project.path)
    } else {
        providesVersions(":aggregation")
        platform(":hiero-dependency-versions")
    }
}

configurations.getByName("mainRuntimeClasspath") {
    attributes.attribute(consistentResolutionAttribute, "global")
}

// In case published versions of a module are also available, always prefer the local one
configurations.all { resolutionStrategy.preferProjectModules() }
