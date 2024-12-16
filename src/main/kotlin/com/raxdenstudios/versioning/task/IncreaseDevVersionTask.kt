package com.raxdenstudios.versioning.task

import com.raxdenstudios.versioning.VersioningPlugin
import com.raxdenstudios.versioning.provider.FileVersionProvider
import org.gradle.api.Project
import org.gradle.kotlin.dsl.register

open class IncreaseDevVersionTask : AbstractIncreaseVersionTask() {

    override fun FileVersionProvider.increaseVersion() {
        increaseDevVersion()
    }


    companion object {

        private const val TASK_NAME = "increaseDevVersion"

        fun register(project: Project) {
            project.tasks.register<IncreaseDevVersionTask>(TASK_NAME) {
                group = VersioningPlugin.GROUP_TASK_NAME
            }
        }
    }
}
