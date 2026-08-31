// SPDX-License-Identifier: Apache-2.0
import org.hiero.gradle.tasks.GitClone

tasks.register<GitClone>("cloneRemoteRepoWTag") {
    description = "Clones a remote Git repository"

    group = "hiero"

    url = "https://github.com/hiero-ledger/hiero-solo-action.git"
    localCloneDirectory = layout.buildDirectory.dir("hiero-solo-action")

    // uncomment below to use a specific tag
    // tag = "v0.53.0" or a specific commit like "0047255"
    tag = "efb0134e921b32ed6302da9c93874d65492e876f"
}