package com.raxdenstudios.versioning.task

import com.raxdenstudios.versioning.version.VersionPart
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.UntrackedTask

/** Increases MINOR by one and resets PATCH and DEV to 0. */
@UntrackedTask(because = "Rewrites the version file in place on every run")
abstract class IncreaseMinorVersionTask : AbstractIncreaseVersionTask() {

    @get:Internal
    override val part: VersionPart = VersionPart.MINOR

    companion object {
        const val TASK_NAME = "increaseMinorVersion"
    }
}
