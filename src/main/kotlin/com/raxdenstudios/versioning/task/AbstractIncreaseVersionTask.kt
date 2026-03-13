package com.raxdenstudios.versioning.task

import com.raxdenstudios.extensionByName
import com.raxdenstudios.printInfo
import com.raxdenstudios.versioning.VersioningPlugin
import com.raxdenstudios.versioning.extension.VersioningExtension
import com.raxdenstudios.versioning.provider.FileVersionProvider
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

abstract class AbstractIncreaseVersionTask : DefaultTask() {

    private val extension: VersioningExtension by lazy {
        project.extensionByName(VersioningPlugin.EXTENSION_NAME)!!
    }

    private val fileVersionProvider: FileVersionProvider by lazy {
        extension.fileVersionProvider
    }

    @TaskAction
    fun execute() {
        val oldVersion = fileVersionProvider.toString()
        fileVersionProvider.increaseVersion()
        val newVersion = fileVersionProvider.toString()
        printInfo("Increasing the patch version $oldVersion -> $newVersion")
    }

    abstract fun FileVersionProvider.increaseVersion()
}
