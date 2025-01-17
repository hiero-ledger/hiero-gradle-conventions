# Hiero Gradle Conventions - Changelog

## Version 0.3.1

* deactivate signing for Gradle plugin publishing if not explicitly turned on
* update spotless-plugin-gradle to 7.0.2 (improves configuration cache compatibility)
* update dependency-analysis-gradle-plugin to 2.7.0 (addresses Gradle deprecation)
* add 'feature.legacy-classpath' convention plugin
* add 'feature.shadow' convention plugin

## Version 0.3.0

* use Prettier for Yaml file formatting
* add license header check for 'module-info' and 'package-info'
* produce code coverage reports as part of 'check'
* always publish build scans on CI
* add 'feature.publish-dependency-constraints' convention plugin
* JPMS - add patching rule: com.squareup:kotlinpoet-jvm

## Version 0.2.1

* JPMS - add patching rule: io.prometheus:prometheus-metrics-config
* JPMS - add patching rule: io.prometheus:prometheus-metrics-core
* JPMS - add patching rule: io.prometheus:prometheus-metrics-exposition-formats
* JPMS - add patching rule: io.prometheus:prometheus-metrics-exposition-textformat
* JPMS - add patching rule: io.prometheus:prometheus-metrics-model
* JPMS - add patching rule: io.prometheus:prometheus-metrics-tracer-common
* JPMS - add patching rule: io.prometheus:prometheus-metrics-tracer-initializer
* JPMS - add patching rule: io.prometheus:prometheus-metrics-tracer-otel
* JPMS - add patching rule: io.prometheus:prometheus-metrics-tracer-otel-agent
* JPMS - add patching rule: io.prometheus:simpleclient_tracer_common
* JPMS - add patching rule: io.micrometer:micrometer-commons
* JPMS - add patching rule: io.micrometer:micrometer-core
* JPMS - add patching rule: io.micrometer:micrometer-observation
* JPMS - add patching rule: io.micrometer:micrometer-registry-prometheus
* JPMS - add patching rule: org.hdrhistogram:HdrHistogram
* JPMS - add patching rule: org.latencyutils:LatencyUtils
* JPMS - remove patching rule: org.json:json

## Version 0.2.0

* JPMS - add patching rule: com.carrotsearch.thirdparty:simple-xml-safe
* JPMS - add patching rule: com.squareup.okhttp3:okhttp
* JPMS - add patching rule: com.squareup.okio:okio-jvm
* JPMS - add patching rule: io.minio:minio
* JPMS - add patching rule: org.xerial.snappy:snappy-java
* JPMS - remove patching rule: commons-codec:commons-codec
* JPMS - remove patching rule: org.jetbrains:annotations
* JPMS - remove patching rule: errorprone (rules were not creating valid modules)
* JPMS - adjust module name: com.google.common.jimfs

## Version 0.1.4

* fix issue caused by interaction between 'releasePrereleaseChannel' and 'publish-artifactregistry' (#55)

## Version 0.1.3

* fix issue caused by interaction between 'publish-maven-central' and 'publish-artifactregistry' (#54)

## Version 0.1.2

* make license header configurable via 'license-header.txt' (#48)
* replace 'developer.properties' with generic Hiero entry (#38)
* update 'io.github.gradle-nexus:publish-plugin' to 2.0.0 (#40)

## Version 0.1.1

* tweak 'publish-maven-central' and add -PpublishTestRelease (#35)
* fix calculation of 'name without version' for rust toolchains (#34)
* include '*.yml' files in spotless YAML check
* expand build group cleanup to tasks added by Kotlin plugins

## Version 0.1.0

* Initial release
