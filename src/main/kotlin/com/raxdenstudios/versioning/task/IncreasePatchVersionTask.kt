package com.raxdenstudios.versioning.task

import com.raxdenstudios.versioning.version.VersionPart
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.UntrackedTask

/** Increases PATCH by one and resets DEV to 0. */
@UntrackedTask(because = "Rewrites the version file in place on every run")
abstract class IncreasePatchVersionTask : AbstractIncreaseVersionTask() {

    @get:Internal
    override val part: VersionPart = VersionPart.PATCH

    companion object {
        const val TASK_NAME = "increasePatchVersion"
    }
}
