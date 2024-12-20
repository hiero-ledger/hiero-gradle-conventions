# Hiero Gradle Conventions - Changelog

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
