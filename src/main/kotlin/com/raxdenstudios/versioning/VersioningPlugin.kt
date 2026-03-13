package com.raxdenstudios.versioning

import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.api.variant.LibraryAndroidComponentsExtension
import com.raxdenstudios.printInfo
import com.raxdenstudios.versioning.extension.AppVersioningExtension
import com.raxdenstudios.versioning.extension.LibraryVersioningExtension
import com.raxdenstudios.versioning.task.GetVersionTask
import com.raxdenstudios.versioning.task.IncreaseDevVersionTask
import com.raxdenstudios.versioning.task.IncreaseMajorVersionTask
import com.raxdenstudios.versioning.task.IncreaseMinorVersionTask
import com.raxdenstudios.versioning.task.IncreasePatchVersionTask
import com.raxdenstudios.versioning.task.ShowVersionInfoTask
import com.raxdenstudios.withApplicationPlugin
import com.raxdenstudios.withDynamicFeaturePlugin
import com.raxdenstudios.withJavaPlatformPlugin
import com.raxdenstudios.withLibraryPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project

class VersioningPlugin : Plugin<Project> {

    companion object {
        const val EXTENSION_NAME = "versioning"
        const val GROUP_TASK_NAME = "versioning"
    }

    override fun apply(project: Project) = with(project) {
        initExtension()
        registerTasks()
        configure()
    }

    private fun Project.initExtension() {
        withApplicationPlugin { AppVersioningExtension.getOrCreate(this) }
        withDynamicFeaturePlugin { AppVersioningExtension.getOrCreate(this) }
        withLibraryPlugin { LibraryVersioningExtension.getOrCreate(this) }
        withJavaPlatformPlugin { LibraryVersioningExtension.getOrCreate(this) }
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
        withApplicationPlugin {
            val androidComponents = extensions.getByType(ApplicationAndroidComponentsExtension::class.java)
            androidComponents.onVariants { variant ->
                val extension = AppVersioningExtension.getOrCreate(this)

                // Make sure these are Providers so they evaluate late
                val vName = providers.provider { extension.versionName }
                val vCode = providers.provider { extension.versionCode }

                variant.outputs.forEach { output ->
                    output.versionName.set(vName)
                    output.versionCode.set(vCode)
                }

                printInfo("Setting versionName to ${extension.versionName}")
                printInfo("Setting versionCode to ${extension.versionCode}")
            }
        }
        withDynamicFeaturePlugin {
            // Intentionally no-op:
            // Dynamic feature modules don't own versionName/versionCode.
            // Versioning comes from the base application module.
        }
        withLibraryPlugin {
            val androidComponents = extensions.getByType(LibraryAndroidComponentsExtension::class.java)
            androidComponents.onVariants { variant ->
                val extension = LibraryVersioningExtension.getOrCreate(this)
                project.version = extension.version

                printInfo("Setting library version to ${extension.version}")
            }
        }
        withJavaPlatformPlugin {
            val extension = LibraryVersioningExtension.getOrCreate(this)
            version = extension.version

            printInfo("Setting platform version to ${extension.version}")
        }
    }
}
