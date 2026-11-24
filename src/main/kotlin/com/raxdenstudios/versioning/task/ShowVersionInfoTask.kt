package com.raxdenstudios.versioning.task

import com.raxdenstudios.versioning.VersioningKind
import com.raxdenstudios.versioning.version.VersionFile
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

/**
 * Prints the configured version file path and the version. Apps get the version name and code,
 * libraries and platforms only the version name. Fails when the version file is missing or invalid.
 */
@DisableCachingByDefault(because = "Only prints the version")
abstract class ShowVersionInfoTask : DefaultTask() {

    // Internal so a missing file reaches VersionFile's message instead of Gradle's input
    // validation; these tasks have no outputs, so they are never up-to-date anyway.
    @get:Internal
    abstract val versionFile: RegularFileProperty

    /** The path as configured in `versioning { filePath = ... }`, for display. */
    @get:Input
    abstract val configuredPath: Property<String>

    /** Decides which lines are printed, see [VersioningKind]. */
    @get:Input
    abstract val kind: Property<VersioningKind>

    @TaskAction
    fun execute() {
        val version = VersionFile.read(versionFile.get().asFile)
        println("  Versioning file: ${configuredPath.get()}")
        when (kind.get()) {
            VersioningKind.APP -> {
                println("  Version name: ${version.versionName}")
                println("  Version code: ${version.versionCode}")
            }

            VersioningKind.LIBRARY -> println("  Version: ${version.versionName}")
        }
    }

    companion object {
        const val TASK_NAME = "showVersionInfo"
    }
}
