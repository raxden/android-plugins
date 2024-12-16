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
        project.extensionByName(VersioningPlugin.EXTENSION_NAME) as VersioningExtension
    }

    private val fileVersionProvider: FileVersionProvider by lazy {
        extension.fileVersionProvider
    }

    @TaskAction
    fun execute() {
        val oldVersion = fileVersionProvider.toString()
        fileVersionProvider.increaseVersion()
        val version = fileVersionProvider.toString()
        printInfo("Increasing the patch version $oldVersion -> $version")
    }

    abstract fun FileVersionProvider.increaseVersion()
}
