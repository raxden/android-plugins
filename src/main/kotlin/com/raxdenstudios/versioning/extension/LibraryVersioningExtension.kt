package com.raxdenstudios.versioning.extension

import com.raxdenstudios.versioning.VersioningPlugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create

open class LibraryVersioningExtension(
    project: Project
) : VersioningExtension(project) {

    val version: String
        get() = fileVersionProvider.versionName

    companion object {

        fun create(project: Project): LibraryVersioningExtension =
            project.extensions.create(VersioningPlugin.EXTENSION_NAME)
    }
}
