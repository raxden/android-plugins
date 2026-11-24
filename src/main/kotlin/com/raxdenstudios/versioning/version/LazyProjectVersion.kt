package com.raxdenstudios.versioning.version

import org.gradle.api.provider.Provider

/**
 * Value assigned to `project.version`. Gradle and publishing plugins call [toString] when they need
 * the version, so the version file is read at that point and not when the plugin is applied.
 *
 * The version is then fixed for the rest of the build: an `increase*` task that runs in the same
 * invocation as a publish does not change the published version. Run them in separate invocations.
 */
class LazyProjectVersion(
    private val versionName: Provider<String>,
) {

    override fun toString(): String = versionName.get()
}
