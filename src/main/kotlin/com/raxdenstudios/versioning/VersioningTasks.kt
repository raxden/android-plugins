package com.raxdenstudios.versioning

import com.raxdenstudios.versioning.extension.VersioningExtension
import com.raxdenstudios.versioning.service.VersionBumpRegistry
import com.raxdenstudios.versioning.task.AbstractIncreaseVersionTask
import com.raxdenstudios.versioning.task.GetVersionTask
import com.raxdenstudios.versioning.task.IncreaseDevVersionTask
import com.raxdenstudios.versioning.task.IncreaseMajorVersionTask
import com.raxdenstudios.versioning.task.IncreaseMinorVersionTask
import com.raxdenstudios.versioning.task.IncreasePatchVersionTask
import com.raxdenstudios.versioning.task.ShowVersionInfoTask
import org.gradle.api.Project
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.register

/**
 * Registers the versioning tasks once, wiring every input from the extension at configuration
 * time so no task touches `Project` at execution time.
 */
internal class VersioningTasks(
    private val project: Project,
    private val extension: VersioningExtension,
    private val versionFile: Provider<RegularFile>,
) {

    private var registered = false

    /**
     * Registers the tasks for [kind]. Only the first call per project registers anything, so the
     * kind of the first applied Android plugin wins.
     */
    fun register(kind: VersioningKind) {
        if (registered) return
        registered = true

        val tasks = project.tasks
        tasks.register<ShowVersionInfoTask>(ShowVersionInfoTask.TASK_NAME) {
            group = VersioningPlugin.GROUP_TASK_NAME
            description = "Prints the version file path and the current version."
            versionFile.set(this@VersioningTasks.versionFile)
            configuredPath.set(extension.filePath)
            this.kind.set(kind)
        }
        tasks.register<GetVersionTask>(GetVersionTask.TASK_NAME) {
            group = VersioningPlugin.GROUP_TASK_NAME
            description = "Prints only the current version name."
            versionFile.set(this@VersioningTasks.versionFile)
        }
        val increases = listOf(
            registerIncrease<IncreaseMajorVersionTask>(IncreaseMajorVersionTask.TASK_NAME, "major"),
            registerIncrease<IncreaseMinorVersionTask>(IncreaseMinorVersionTask.TASK_NAME, "minor"),
            registerIncrease<IncreasePatchVersionTask>(IncreasePatchVersionTask.TASK_NAME, "patch"),
            registerIncrease<IncreaseDevVersionTask>(IncreaseDevVersionTask.TASK_NAME, "dev"),
        )
        // Deterministic order when several run in one build (e.g. `increaseMinorVersion
        // increasePatchVersion` -> 1.4.1), regardless of command-line order or parallelism.
        increases.forEachIndexed { index, task ->
            task.configure { mustRunAfter(increases.take(index)) }
        }
    }

    private inline fun <reified T : AbstractIncreaseVersionTask> registerIncrease(
        name: String,
        label: String,
    ): TaskProvider<T> {
        val registry = VersionBumpRegistry.register(project)
        return project.tasks.register<T>(name) {
            group = VersioningPlugin.GROUP_TASK_NAME
            description = "Increases the $label version in the version file."
            versionFile.set(this@VersioningTasks.versionFile)
            bumpRegistry.set(registry)
            usesService(registry)
        }
    }
}
