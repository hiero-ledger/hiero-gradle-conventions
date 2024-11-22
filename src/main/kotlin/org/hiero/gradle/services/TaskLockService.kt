// SPDX-License-Identifier: Apache-2.0
package org.hiero.gradle.services

import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters

abstract class TaskLockService : BuildService<BuildServiceParameters.None>
