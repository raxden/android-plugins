package com.raxdenstudios.versioning.task

import com.raxdenstudios.versioning.VersioningPlugin
import com.raxdenstudios.versioning.provider.FileVersionProvider
import org.gradle.api.Project
import org.gradle.kotlin.dsl.register

open class IncreasePatchVersionTask : AbstractIncreaseVersionTask() {

    override fun FileVersionProvider.increaseVersion() {
        increasePatchVersion()
        resetDevVersion()
    }


    companion object {

        private const val TASK_NAME = "increasePatchVersion"

        fun register(project: Project) {
            project.tasks.register<IncreasePatchVersionTask>(TASK_NAME) {
                group = VersioningPlugin.GROUP_TASK_NAME
            }
        }
    }
}
