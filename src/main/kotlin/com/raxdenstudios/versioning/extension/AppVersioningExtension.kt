package com.raxdenstudios.versioning.extension

import com.raxdenstudios.getOrCreateExtension
import com.raxdenstudios.versioning.VersioningPlugin
import org.gradle.api.Project

open class AppVersioningExtension(
    project: Project
) : VersioningExtension(project) {

    val versionName: String
        get() = fileVersionProvider.versionName
    val versionCode: Int
        get() = fileVersionProvider.versionCode

    companion object {

        fun getOrCreate(project: Project): AppVersioningExtension =
            project.getOrCreateExtension(VersioningPlugin.EXTENSION_NAME)
    }
}
