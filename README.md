[![Build Status](https://img.shields.io/endpoint.svg?url=https%3A%2F%2Factions-badge.atrox.dev%2Fhiero-ledger%2Fhiero-gradle-conventions%2Fbadge%3Fref%3Dmain&style=flat)](https://actions-badge.atrox.dev/hiero-ledger/hiero-gradle-conventions/goto?ref=main)
[![Gradle Plugin Portal](https://img.shields.io/maven-metadata/v?label=Plugin%20Portal&metadataUrl=https%3A%2F%2Fplugins.gradle.org%2Fm2%2Forg%2Fhiero%2Fgradle%2Fhiero-gradle-conventions%2Fmaven-metadata.xml)](https://plugins.gradle.org/plugin/org.hiero.gradle.build)
[![License](https://img.shields.io/badge/license-apache2-blue.svg)](LICENSE)

# Hiero Gradle Conventions

Gradle [convention plugins](https://docs.gradle.org/current/samples/sample_convention_plugins.html) used by Hiero projects.

Any Gradle-based Java project that uses the Java Module System (JPMS) may use these convention plugins.
That includes projects that are not part of the Hiero organisation. The conventions follow latest Gradle best practices
and ensure compatibility with performance features such as the
[Remote Build Cache](https://docs.gradle.com/build-cache-node/) and the
[Configuration Cache](https://docs.gradle.org/current/userguide/configuration_cache.html).
The convention plugins pull in a [curated set of third-party Gradle plugins](build.gradle.kts#L18-L34)
that also support these features and are compatible with the latest Gradle version.

## Using the Convention Plugins

Apply the entry point plugin `org.hiero.gradle.build` in the `settings.gradle.kts` file.  Additionally, define where
Modules (subprojects) are located in the directory hierarchy by using the `javaModules { ... }` notation
(which is provided by the [org.gradlex.java-module-dependencies](https://github.com/gradlex-org/java-module-dependencies?tab=readme-ov-file#project-structure-definition-when-using-this-plugin-as-settings-plugin) plugin).

```
// settings.gradle.kts
plugins {
    id("org.hiero.gradle.build") version "0.1.0"
}

// Define location of Modules (subprojects)
javaModules {
    directory("product-a") { // searches for 'module-info.java' in subfolders of 'product-a'
        group = "org.example.product-a"
    }
}
```

In each Module (subproject), apply one of the `org.hiero.gradle.module.*` plugins and, if desired, additional
`org.hiero.gradle.feature.*` plugins.

For example, to define a Library Module that also provides _test fixtures_ and has _JMH benchmarks_, the plugins block
should look like this:

```
plugins {
    id("org.hiero.gradle.module.library")
    id("org.hiero.gradle.feature.test-fixtures")
    id("org.hiero.gradle.feature.benchmark")
}
```

### Project structure

There is a [minimal example](example) setup.

```
├── settings.gradle.kts                          // Entriy point (see above)
├── gradle.properties                            // Turn on Gradle caches
├── gradle/aggregation/build.gradle.kts          // List of all product/service modules for consistent resolution
├── gradle/toolchain-versions.properties         // JDK version (and other tools if applicable)
├── gradle/wrapper/gradle-wrapper.properties     // Gradle version (defined through Gradle wrapper)
├── hiero-dependency-versions/build.gradle.kts   // Versions of 3rd-party modules
├── product-a                                    // Folder containing all modules of 'product-a'
│   ├── module-app                               // Example of a Application module
│   │   ├── build.gradle.kts                     // Select which build features to use in 'plugins {}' (see above)
│   │   └── src/main/java/module-info.java       // Define dependencies to other modules
│   ├── module-lib                               // Example of a Library module
│   │   ├── build.gradle.kts                     // Select which build features to use in 'plugins {}' (see above)
│   │   └── src/main/java/module-info.java       // Define dependencies to other modules
│   └── description.txt                          // Description of the product (for published metadata),
│                                                //   or set 'description' for individual module in build.gradle.kts
└── version.txt                                  // Version of all modules/products
```

## List of Convention Plugins

The plugins are written in Gradle's Kotlin DSL and are found in [src/main/kotlin](src/main/kotlin).
Each plugin has a short description located in [src/main/descriptions](src/main/descriptions).

Each plugin configures a certain build aspect, following this naming pattern:

- `org.hiero.gradle.base.*` _Base_ plugins need to be used in all Modules to establish a certain foundation
  for the setup. For example, the same dependency management configuration should be applied everywhere to
  consistently use the same 3rd party libraries everywhere.
- `org.hiero.gradle.feature.*` Each _feature_ plugin configures one aspect of building the software –
  like compiling code or testing code.
- `org.hiero.gradle.check.*` _Check_ plugins help with keeping the software maintainable over time.
  They check things like the dependency setup or code formatting.
- `org.hiero.gradle.report.*` _Report_ plugins configure the export of information into reports that can be picked
  up by other systems - e.g. for code coverage reporting.
- `org.hiero.gradle.module.*` _Module_ plugins combine plugins from all categories above to define
  _Module Types_ that are then used in the `build.gradle.kts` files of the individual Modules of our software.

## Contributing

Whether you’re fixing bugs, enhancing features, or improving documentation, your contributions are important — let’s build something great together!

Please read our [contributing guide](https://github.com/hiero-ledger/.github/blob/main/CONTRIBUTING.md) to see how you can get involved.

### Making and testing changes

#### Use local changes to plugins in a project

Insert the line
`pluginManagement { includeBuild("<path-to-hiero-gradle-conventions>") }`
in the top of your `settings.gradle.kts`. For example, if this repository is cloned next to the project repository in
your local file system, the top part of your `settings.gradle.kts` should look like this:

```
// SPDX-License-Identifier: Apache-2.0
pluginManagement { includeBuild("../hiero-gradle-conventions") }

plugins { id("org.hiero.gradle.build") version "0.1.0" }
```

After you inserted that line, reload your project in IntelliJ. You will now see `hiero-gradle-conventions`
next to your project in the workspace. You can now make changes to the files in [src/main/kotlin](src/main/kotlin).
Your changes are automatically used when running a build.

#### Add or adjust a test

Each change done to the plugins should be covered by a test. The tests are located in
[src/test/kotlin](src/main/kotlin). They are written in Kotlin using [JUnit5](https://junit.org/junit5/), 
[AssertJ](https://assertj.github.io/doc/) and [Gradle Test Kit](https://docs.gradle.org/current/userguide/test_kit.html).
Each test creates an artificial project that applies the plugin(s) under test, runs a build and asserts build results –
such as: state of tasks executed, console logging, created files. Take a look at the existing tests for more details.

## Code of Conduct

Hiero uses the Linux Foundation Decentralised Trust [Code of Conduct](https://www.lfdecentralizedtrust.org/code-of-conduct).

## License

[Apache License 2.0](LICENSE)
