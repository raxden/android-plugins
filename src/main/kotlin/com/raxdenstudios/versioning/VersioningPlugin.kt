package com.raxdenstudios.versioning

import com.android.build.gradle.BaseExtension
import com.raxdenstudios.printInfo
import com.raxdenstudios.versioning.extension.AppVersioningExtension
import com.raxdenstudios.versioning.extension.LibraryVersioningExtension
import com.raxdenstudios.versioning.extension.VersioningExtension
import com.raxdenstudios.versioning.task.GetVersionTask
import com.raxdenstudios.versioning.task.IncreaseDevVersionTask
import com.raxdenstudios.versioning.task.IncreaseMajorVersionTask
import com.raxdenstudios.versioning.task.IncreaseMinorVersionTask
import com.raxdenstudios.versioning.task.IncreasePatchVersionTask
import com.raxdenstudios.versioning.task.ShowVersionInfoTask
import com.raxdenstudios.withApplicationPlugin
import com.raxdenstudios.withDynamicFeaturePlugin
import com.raxdenstudios.withLibraryPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType

class VersioningPlugin : Plugin<Project> {

    companion object {
        const val EXTENSION_NAME = "versioning"
        const val GROUP_TASK_NAME = "versioning"
    }

    private lateinit var extension: VersioningExtension

    override fun apply(project: Project) = with(project) {
        initExtension()
        registerTasks()
        configure()
    }

    private fun Project.initExtension() {
        withApplicationPlugin { extension = AppVersioningExtension.create(this) }
        withDynamicFeaturePlugin { extension = AppVersioningExtension.create(this) }
        withLibraryPlugin { extension = LibraryVersioningExtension.create(this) }
    }

    private fun Project.registerTasks() {
        ShowVersionInfoTask.register(this)
        GetVersionTask.register(this)
        IncreaseMajorVersionTask.register(this)
        IncreaseMinorVersionTask.register(this)
        IncreasePatchVersionTask.register(this)
        IncreaseDevVersionTask.register(this)
    }

    private fun Project.configure() {
        afterEvaluate {
            extensions.getByType<BaseExtension>().run {
                withApplicationPlugin {
                    val extension = extension as AppVersioningExtension
                    defaultConfig.versionName = extension.versionName
                    defaultConfig.versionCode = extension.versionCode
                    printInfo("Setting versionName to ${extension.versionName}")
                    printInfo("Setting versionCode to ${extension.versionCode}")
                }
                withDynamicFeaturePlugin {
                    val extension = extension as AppVersioningExtension
                    defaultConfig.versionName = extension.versionName
                    defaultConfig.versionCode = extension.versionCode
                    printInfo("Setting versionName to ${extension.versionName}")
                    printInfo("Setting versionCode to ${extension.versionCode}")
                }
                withLibraryPlugin {
                    val extension = extension as LibraryVersioningExtension
                    version = extension.version
                    printInfo("Setting library version to ${extension.version}")
                }
            }
        }
    }
}
