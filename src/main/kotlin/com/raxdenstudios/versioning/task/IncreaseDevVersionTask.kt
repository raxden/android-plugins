package com.raxdenstudios.versioning.task

import com.raxdenstudios.versioning.version.VersionPart
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.UntrackedTask

/** Increases DEV by one. The other parts are not changed. */
@UntrackedTask(because = "Rewrites the version file in place on every run")
abstract class IncreaseDevVersionTask : AbstractIncreaseVersionTask() {

    @get:Internal
    override val part: VersionPart = VersionPart.DEV

    companion object {
        const val TASK_NAME = "increaseDevVersion"
    }
}
