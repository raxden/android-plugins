package com.raxdenstudios.versioning.task

import com.raxdenstudios.versioning.version.VersionFile
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

/**
 * Prints only the version name to stdout, so CI can read it with `./gradlew -q getVersion | tail -n 1`.
 */
@DisableCachingByDefault(because = "Only prints the version")
abstract class GetVersionTask : DefaultTask() {

    // Internal so a missing file reaches VersionFile's message instead of Gradle's input
    // validation; these tasks have no outputs, so they are never up-to-date anyway.
    @get:Internal
    abstract val versionFile: RegularFileProperty

    @TaskAction
    fun execute() {
        println(VersionFile.read(versionFile.get().asFile).versionName)
    }

    companion object {
        const val TASK_NAME = "getVersion"
    }
}
