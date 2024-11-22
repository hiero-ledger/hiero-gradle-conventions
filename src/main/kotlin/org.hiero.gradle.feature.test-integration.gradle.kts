// SPDX-License-Identifier: Apache-2.0
plugins { id("java") }

// Tests that could be in the default "test" set but take more than 100ms to execute.
@Suppress("UnstableApiUsage")
testing.suites {
    register<JvmTestSuite>("testIntegration") { testType = TestSuiteType.INTEGRATION_TEST }
}
