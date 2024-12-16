package com.raxdenstudios.versioning.task

import com.raxdenstudios.versioning.VersioningPlugin
import com.raxdenstudios.versioning.provider.FileVersionProvider
import org.gradle.api.Project
import org.gradle.kotlin.dsl.register

open class IncreaseMinorVersionTask : AbstractIncreaseVersionTask() {

    override fun FileVersionProvider.increaseVersion() {
        increaseMinorVersion()
        resetPatchVersion()
        resetDevVersion()
    }

    companion object {

        private const val TASK_NAME = "increaseMinorVersion"

        fun register(project: Project) {
            project.tasks.register<IncreaseMinorVersionTask>(TASK_NAME) {
                group = VersioningPlugin.GROUP_TASK_NAME
            }
        }
    }
}
