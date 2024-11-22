// SPDX-License-Identifier: Apache-2.0
import com.adarshr.gradle.testlogger.theme.ThemeType

plugins { id("com.adarshr.test-logger") }

testlogger {
    theme = ThemeType.MOCHA_PARALLEL
    slowThreshold = 10000
    showPassed = false
    showSkipped = false
    showStandardStreams = true
    showPassedStandardStreams = false
    showSkippedStandardStreams = false
    showFailedStandardStreams = true
}
