package com.raxdenstudios.versioning.task

import com.raxdenstudios.versioning.VersioningPlugin
import com.raxdenstudios.versioning.provider.FileVersionProvider
import org.gradle.api.Project
import org.gradle.kotlin.dsl.register

open class IncreaseMajorVersionTask : AbstractIncreaseVersionTask() {

    override fun FileVersionProvider.increaseVersion() {
        increaseMajorVersion()
        resetMinorVersion()
        resetPatchVersion()
        resetDevVersion()
    }

    companion object {

        private const val TASK_NAME = "increaseMajorVersion"

        fun register(project: Project) {
            project.tasks.register<IncreaseMajorVersionTask>(TASK_NAME) {
                group = VersioningPlugin.GROUP_TASK_NAME
            }
        }
    }
}
