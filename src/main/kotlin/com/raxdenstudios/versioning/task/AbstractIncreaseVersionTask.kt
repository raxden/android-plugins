package com.raxdenstudios.versioning.task

import com.raxdenstudios.versioning.service.VersionBumpRegistry
import com.raxdenstudios.versioning.version.VersionFile
import com.raxdenstudios.versioning.version.VersionPart
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.UntrackedTask
import java.io.File

/**
 * Base of the `increase*Version` tasks. Each task increases one [VersionPart] of the version file:
 * read, [Version.increase], validate, write. The first task that claims a file and part in the
 * build performs the increase; later ones log a skip. A failed increase releases its claim, so
 * another task for the same part can still run. Throws [GradleException] when the file is missing
 * or invalid, or when the increase would produce an invalid version.
 */
@UntrackedTask(because = "Rewrites the version file in place on every run")
abstract class AbstractIncreaseVersionTask : DefaultTask() {

    /** Version file, both read and written by this task. */
    @get:Internal
    abstract val versionFile: RegularFileProperty

    /** Build-scoped claims shared by all increase tasks, see [VersionBumpRegistry]. */
    @get:Internal
    abstract val bumpRegistry: Property<VersionBumpRegistry>

    @get:Internal
    protected abstract val part: VersionPart

    @TaskAction
    fun execute() {
        val file = versionFile.get().asFile
        val registry = bumpRegistry.get()
        val alreadyIncreasedBy = registry.claim(file, part, path)
        if (alreadyIncreasedBy != null) {
            logger.lifecycle(
                "Skipping: ${part.label} version of ${file.path} was already increased in this build " +
                    "by $alreadyIncreasedBy"
            )
            return
        }
        try {
            increase(file)
        } catch (failure: Exception) {
            registry.release(file, part, path)
            throw failure
        }
    }

    private fun increase(file: File) {
        val current = VersionFile.read(file)
        val next = current.increase(part)
        val errors = next.validationErrors()
        if (errors.isNotEmpty()) {
            throw GradleException(
                "Cannot increase ${part.label} version of '${file.path}' from $current: ${errors.joinToString("; ")}"
            )
        }
        VersionFile.write(file, next)
        logger.lifecycle("Increasing ${part.label} version $current -> $next")
    }
}
