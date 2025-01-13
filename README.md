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
The convention plugins pull in a [curated set of third-party Gradle plugins](build.gradle.kts#L12-L29)
that also support these features and are compatible with the latest Gradle version.

## ToC

- [Using the Convention Plugins](#using-the-convention-plugins) (for project maintainers)
- [List of Convention Plugins](#list-of-convention-plugins) (for project maintainers)
- [Building a project that uses these plugins](#building-a-project-that-uses-these-plugins) (for developers)
  - [From the command line](#from-the-command-line)
  - [In IntelliJ](#in-intellij)
  - [In GitHub Actions](#in-github-actions)

## Using the Convention Plugins

Apply the entry point plugin `org.hiero.gradle.build` in the `settings.gradle.kts` file.  Additionally, define where
Modules (subprojects) are located in the directory hierarchy by using the `javaModules { ... }` notation
(which is provided by the [org.gradlex.java-module-dependencies](https://github.com/gradlex-org/java-module-dependencies?tab=readme-ov-file#project-structure-definition-when-using-this-plugin-as-settings-plugin) plugin).

```
// settings.gradle.kts
plugins {
    id("org.hiero.gradle.build") version "0.2.1"
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

## Building a project that uses these plugins

## From the command line

Run `./gradlw` to get the list of tasks that are useful to check and test local changes:

```
Build tasks
-----------
assemble - Assembles the outputs of this project.
build - Assembles and tests this project.
qualityGate - Apply spotless rules and run all quality checks.
test - Runs the test suite.
```

In addition, the following build parameters may be useful:

|  Task  |               Parameter                |                          Description                           |                Remarks                 |
|--------|----------------------------------------|----------------------------------------------------------------|----------------------------------------|
| `test` | `-PactiveProcessorCount=<proc-number>` | not run tests in parallel and reduce number of processors used |                                        |
| `jmh`  | `-PjmhTests=<includes>`                | select benchmarks to run - e.g. `com.example.jmh.Benchmark1`   | only projects with `feature.benchmark` |

## In IntelliJ

Open the root folder of the project in IntelliJ. It is automatically recognized as Gradle project and imported.

### Configure the JDK used by Gradle

Before you can use all features reliably, make sure that Gradle is started with the JDK used in the project.
The JDK version of the project is defined in `gradle/toolchain-versions.properties`.

You can use IntelliJ to download the JDK if you do not have it installed.

![Configuring the JDK in IntelliJ](src/docs/assets/gradle-jdk.png)

## Reload Project with Gradle

After you changed something in the project setup you should press the **Reload All Gradle project** in IntelliJ.
Changes to the project setup include:

- Changing build setup/plugins in `build.gradle.kts` files
- Changing dependencies in `src/main/java/module-info.java` files
- Changing dependency versions in `hiero-dependency-versions/build.gradle.kts`

![Configuring the JDK in IntelliJ](src/docs/assets/gradle-reload.png)

## Build, test, run through Gradle tasks

You can run all tasks (see [command line](#from-the-command-line)) from the Gradle tool window.
Usually, you only require the tasks listed in the **build** group.

![Configuring the JDK in IntelliJ](src/docs/assets/gradle-tasks.png)

To run only a single test, you can use the _run test_ options offered by IntelliJ when you open a test file.
IntelliJ then automatically constructs the required Gradle call to run the test through Gradle.

## In GitHub Actions

GitHub action pipelines should use the official [setup-gradle action](https://github.com/gradle/actions/) with the following tasks and parameters.

### Building and testing

In a CI pipeline for PR validation with multiple steps use the following.
(The available _test sets_ are determined by the additional `feature.test-*` plugins used in the project.)

|       Task and Parameters       |                              Description                               |
|---------------------------------|------------------------------------------------------------------------|
| `./gradlew assemble --scan`     | Build all artifacts (populates remote build cache)                     |
| `./gradlew qualityCheck --scan` | Run all checks except tests                                            |
| `./gradlew test --scan`         | Run all unit tests                                                     |
| `./gradlew <test-set> --scan`   | Run all tests in _test-set_ (possibly on different agents in parallel) |

Alternatively, if you are fine to do more in one pipeline step, you can use:

|      Task and Parameters      |             Description             |
|-------------------------------|-------------------------------------|
| `./gradlew build --scan`      | `assemble` + `qualiyCheck` + `test` |
| `./gradlew <test-set> --scan` | Run all tests in _test-set_         |

#### Environment

The following environment variables should be populated from _secrets_ to ensure a performant build.

|      Env Variable       |             Description              |
|-------------------------|--------------------------------------|
| `GRADLE_CACHE_USERNAME` | Gradle remote build cache _username_ |
| `GRADLE_CACHE_PASSWORD` | Gradle remote build cache _password_ |

### Code coverage reports

Running `test` produces code coverage data. The following creates a single XML file with all coverage data for upload
to coverage analysis services like Codecov.

|            Task and Parameters            |                 Description                  |
|-------------------------------------------|----------------------------------------------|
| `./gradlew testCodeCoverageReport --scan` | Generate a single XML with all coverage data |

Report location: `gradle/aggregation/build/reports/jacoco/testCodeCoverageReport/testCodeCoverageReport.xml`

### Publishing

Before doing the publishing, you may need to update the version (version.txt file) in a preceding step.

|                     Task and Parameters                     |               Description                |
|-------------------------------------------------------------|------------------------------------------|
| `./gradlew versionAsSpecified -PnewVersion=<version>`       | Define version to store in version.txt   |
| `./gradlew versionAsSnapshot`                               | Add _-SNAPSHOT_ suffix to version.txt    |
| `./gradlew versionAsPrefixedCommit -PcommitPrefix=<prefix>` | Set version based on current commit hash |

To perform the actual publishing use one of the following.
(If multiple _products_ with different _groups_ should be published, the `releaseMavenCentral` task needs to run
multiple times with different values for the `publishingPackageGroup` parameter.)

|                                       Task and Parameters                                        |              Description               |
|--------------------------------------------------------------------------------------------------|----------------------------------------|
| `./gradlew releaseMavenCentral -PpublishingPackageGroup=<group> --no-configuration-cache --scan` | Publish artifacts to Maven central     |
| `./gradlew publishPlugins --no-configuration-cache --scan`                                       | Publish plugin to Gradle plugin portal |

The following parameters may be used to tune or test the publishing (default is `false` for all parameters).

|           Task and Parameters           |                     Description                     |
|-----------------------------------------|-----------------------------------------------------|
| `-PpublishSigningEnabled=<true\|false>` | Set to `true` for actual publishing                 |
| `-Ps01SonatypeHost=<true\|false>`       | Use the `s01.oss.sonatype.org` host if required     |
| `-PpublishTestRelease=<true\|false>`    | `false` - auto-release from staging when successful |

The following environment variables should be populated from _secrets_ to ensure a fully functional build.

#### Environment

|      Env Variable       |               Description               |
|-------------------------|-----------------------------------------|
| `NEXUS_USERNAME`        | Maven Central publish _username_        |
| `NEXUS_PASSWORD`        | Maven Central publish _password_        |
| `GRADLE_PUBLISH_KEY`    | Gradle plugin portal publish _username_ |
| `GRADLE_PUBLISH_SECRET` | Gradle plugin portal publish _password_ |

### Testing Rust code on multiple operating systems

If `feature.rust` and `feature.test-multios` is used, you can configure a matrix pipeline to run `test` on multiple
agents with different operating systems. In this case, you can use the following parameter to skip the rust installation
on the test-only agents where compiled code is retrieved from the remote build cache.

|                Env Variable                 |                                 Description                                 |
|---------------------------------------------|-----------------------------------------------------------------------------|
| `-PskipInstallRustToolchains=<true\|false>` | Skip `installRustToolchains` task if all `cargoBuild*` tasks are FROM-CACHE |

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
