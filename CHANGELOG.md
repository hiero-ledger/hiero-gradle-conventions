# Hiero Gradle Conventions - Changelog

## Version 0.2.0

* replace 'developer.properties' with generic Hiero entry (#38)
* update 'io.github.gradle-nexus:publish-plugin' to 2.0.0 (#40)
* JPMS - remove patching rule: commons-codec:commons-codec
* JPMS - remove patching rule: org.jetbrains:annotations
* JPMS - remove patching rule: org.json:json
* JPMS - remove patching rule: errorprone (rules were not creating valid modules)
* JPMS - adjust module name: com.google.common.jimfs

## Version 0.1.1

* tweak 'publish-maven-central' and add -PpublishTestRelease (#35)
* fix calculation of 'name without version' for rust toolchains (#34)
* include '*.yml' files in spotless YAML check
* expand build group cleanup to tasks added by Kotlin plugins

## Version 0.1.0

* Initial release
