package com.raxdenstudios.versioning.service

import com.raxdenstudios.versioning.version.VersionPart
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters
import java.io.File
import java.util.concurrent.ConcurrentHashMap

/**
 * Build-scoped record of the version parts already increased in the current build, so a version
 * file shared by several modules gets each `increase*Version` applied only once, while different
 * parts (e.g. `increaseMinorVersion increasePatchVersion`) still run in sequence.
 *
 * The registry lives as long as the build and is scoped to the plugin class loader: projects that
 * load the plugin through different class loaders do not see each other's claims.
 */
abstract class VersionBumpRegistry : BuildService<BuildServiceParameters.None> {

    private val claims = ConcurrentHashMap<String, String>()

    /**
     * Atomically claims the increase of [part] in [file] for [taskPath]. Returns `null` when
     * claimed, or the path of the task that already claimed it in this build.
     */
    fun claim(file: File, part: VersionPart, taskPath: String): String? =
        claims.putIfAbsent(key(file, part), taskPath)

    /** Releases a claim made by [taskPath], e.g. when the increase failed and wrote nothing. */
    fun release(file: File, part: VersionPart, taskPath: String) {
        claims.remove(key(file, part), taskPath)
    }

    private fun key(file: File, part: VersionPart): String = "${file.canonicalPath}#${part.key}"

    companion object {

        // Scoped to the plugin class loader: a service registered by another class loader
        // would have an incompatible type.
        private val SERVICE_NAME =
            "versioningBumpRegistry-${System.identityHashCode(VersionBumpRegistry::class.java)}"

        /**
         * One usage at a time: increase tasks read-modify-write the file, so two of them running in
         * parallel (the configuration cache runs tasks of one project in parallel) would lose updates.
         */
        fun register(project: Project): Provider<VersionBumpRegistry> =
            project.gradle.sharedServices.registerIfAbsent(SERVICE_NAME, VersionBumpRegistry::class.java) {
                maxParallelUsages.set(1)
            }
    }
}
