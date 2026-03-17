// SPDX-License-Identifier: Apache-2.0
plugins { id("org.hiero.gradle.build") version "0.7.6" }

rootProject.name = "example-repository"

javaModules { directory("product-a") { group = "org.example.product-a" } }
