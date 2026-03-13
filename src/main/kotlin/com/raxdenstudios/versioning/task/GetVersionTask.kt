package com.raxdenstudios.versioning.task

import com.raxdenstudios.extensionByName
import com.raxdenstudios.versioning.VersioningPlugin
import com.raxdenstudios.versioning.extension.AppVersioningExtension
import com.raxdenstudios.versioning.extension.LibraryVersioningExtension
import com.raxdenstudios.versioning.extension.VersioningExtension
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.register

open class GetVersionTask : DefaultTask() {

    private val extension: VersioningExtension by lazy {
        project.extensionByName(VersioningPlugin.EXTENSION_NAME)!!
    }

    @TaskAction
    fun execute() {
        when (val extension = extension) {
            is AppVersioningExtension -> println(extension.versionName)
            is LibraryVersioningExtension -> println(extension.version)
            else -> error("Versioning extension not supported")
        }
    }

    companion object {

        private const val TASK_NAME = "getVersion"

        fun register(project: Project) {
            project.tasks.register<GetVersionTask>(TASK_NAME) {
                group = VersioningPlugin.GROUP_TASK_NAME
            }
        }
    }
}
