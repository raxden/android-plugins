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

open class ShowVersionInfoTask : DefaultTask() {

    private val extension: VersioningExtension by lazy {
        project.extensionByName(VersioningPlugin.EXTENSION_NAME)!!
    }

    @TaskAction
    fun execute() {
        println("  Versioning file: ${extension.filePath}")
        when (val extension = extension) {
            is AppVersioningExtension -> {
                println("  Version name: ${extension.versionName}")
                println("  Version code: ${extension.versionCode}")
            }

            is LibraryVersioningExtension -> {
                println("  Version: ${extension.version}")
            }
        }
    }

    companion object {

        private const val TASK_NAME = "showVersionInfo"

        fun register(project: Project) {
            project.tasks.register<ShowVersionInfoTask>(TASK_NAME) {
                group = VersioningPlugin.GROUP_TASK_NAME
            }
        }
    }
}
