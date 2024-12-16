package com.raxdenstudios.versioning.extension

import com.raxdenstudios.versioning.VersioningPlugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create

open class AppVersioningExtension(
    project: Project
): VersioningExtension(project) {

    val versionName: String
        get() = fileVersionProvider.versionName
    val versionCode: Int
        get() = fileVersionProvider.versionCode

    companion object {

        fun create(project: Project): AppVersioningExtension =
            project.extensions.create(VersioningPlugin.EXTENSION_NAME)
    }
}
