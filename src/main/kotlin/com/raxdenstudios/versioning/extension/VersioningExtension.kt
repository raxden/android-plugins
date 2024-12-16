package com.raxdenstudios.versioning.extension

import com.raxdenstudios.versioning.provider.FileVersionProvider
import org.gradle.api.Project
import javax.inject.Inject

open class VersioningExtension @Inject constructor(
    val project: Project
) {

    companion object {
        const val DEFAULT_FILE_PATH = "./version.properties"
    }

    var filePath: String = DEFAULT_FILE_PATH
    val fileVersionProvider by lazy { FileVersionProvider(filePath) }
}
