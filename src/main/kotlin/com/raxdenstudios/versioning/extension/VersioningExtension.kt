package com.raxdenstudios.versioning.extension

import org.gradle.api.provider.Property

/**
 * The `versioning {}` block added by [com.raxdenstudios.versioning.VersioningPlugin]. It only sets
 * where the version file is; the file itself is the source of truth for the version.
 *
 * ```kotlin
 * versioning {
 *     filePath = "version.properties"
 * }
 * ```
 */
abstract class VersioningExtension {

    /**
     * Path of the version file, relative to the project directory. Defaults to [DEFAULT_FILE_PATH].
     * Read lazily, so it can be configured anywhere in the build script.
     */
    abstract val filePath: Property<String>

    init {
        filePath.convention(DEFAULT_FILE_PATH)
    }

    companion object {
        const val DEFAULT_FILE_PATH = "version.properties"
    }
}
