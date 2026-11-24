package com.raxdenstudios.versioning.version

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters

/**
 * Contents of the version file as a configuration cache input: Gradle runs [obtain] again before
 * reusing a cache entry, so editing the file (e.g. `increasePatchVersion`) invalidates it.
 * Obtaining fails with the same missing-file error as [VersionFile.read] when the file is absent.
 */
abstract class VersionFileValueSource : ValueSource<String, VersionFileValueSource.Parameters> {

    interface Parameters : ValueSourceParameters {
        val versionFile: RegularFileProperty
    }

    override fun obtain(): String {
        val file = parameters.versionFile.get().asFile
        if (!file.isFile) throw VersionFile.missingFile(file.path)
        return file.readText()
    }
}
