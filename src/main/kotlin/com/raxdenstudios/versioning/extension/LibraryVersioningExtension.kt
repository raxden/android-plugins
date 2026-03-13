package com.raxdenstudios.versioning.extension

import com.raxdenstudios.getOrCreateExtension
import com.raxdenstudios.versioning.VersioningPlugin
import org.gradle.api.Project

open class LibraryVersioningExtension(
    project: Project
) : VersioningExtension(project) {

    val version: String
        get() = fileVersionProvider.versionName

    companion object {

        fun getOrCreate(project: Project): LibraryVersioningExtension =
            project.getOrCreateExtension(VersioningPlugin.EXTENSION_NAME)
    }
}
