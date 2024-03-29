package com.raxdenstudios.releasing.task

import com.raxdenstudios.checkoutBranch
import org.ajoberstar.grgit.Grgit
import org.gradle.api.tasks.TaskAction

open class ReleaseCandidateTagTask : AbstractReleaseCandidateTask() {

    @TaskAction
    fun execute() {
        openGitWithCredentials().run {
            checkoutBranch(getReleaseBranch())
            createTagRelease()
            bumpVersion()
            checkoutBranch(MASTER_BRANCH)
            close()
        }
    }

    private fun Grgit.createTagRelease() {
        tag.add { name = getTagName() }
        push { tags = true }
    }

    private fun Grgit.bumpVersion() {
        increasePatchVersion()
        add { patterns = mutableSetOf(".") }
        commit { message = getCommitBumpVersionMessage() }
        push()
    }
}
