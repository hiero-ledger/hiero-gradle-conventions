// SPDX-License-Identifier: Apache-2.0
package org.hiero.gradle.tasks

import java.io.File

internal object CargoUtil {

    /*
     * Clean the global Cargo cache to not have it as (unstable) part
     * of the output of the RustToolchainInstallTask.
     */
    fun cleanCache(cargoFolder: File) {
        File(cargoFolder, ".global-cache").delete()
        File(cargoFolder, "registry/cache").deleteRecursively()
    }
}
