package com.raxdenstudios.versioning

import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.raxdenstudios.versioning.extension.VersioningExtension
import com.raxdenstudios.versioning.version.LazyProjectVersion
import com.raxdenstudios.versioning.version.Version
import com.raxdenstudios.versioning.version.VersionFile
import com.raxdenstudios.versioning.version.VersionFileValueSource
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider

/**
 * Gradle plugin `com.raxdenstudios.android-versioning`. Reads the version from a
 * `version.properties` file (keys `MAJOR`, `MINOR`, `PATCH`, `DEV`) and applies it to the module:
 *
 * - `com.android.application`: sets `versionName` and `versionCode` on every output variant.
 * - `com.android.dynamic-feature`: registers the tasks only, the version comes from the base app.
 * - `com.android.library` and `java-platform`: sets `project.version` to the version name.
 *
 * The plugin registers `showVersionInfo`, `getVersion` and the `increase{Major,Minor,Patch,Dev}Version`
 * tasks, grouped under `versioning`.
 *
 * ## Setup
 *
 * ```kotlin
 * plugins {
 *     id("com.raxdenstudios.android-versioning")
 * }
 * ```
 *
 * ## Usage
 *
 * ```sh
 * ./gradlew -q getVersion          # prints only the version name, for CI
 * ./gradlew increasePatchVersion   # rewrites the version file in place
 * ```
 *
 * The version is read once per build. Run `increase*` tasks and publishing in separate
 * invocations: a publish in the same invocation as an increase still uses the old version.
 */
class VersioningPlugin : Plugin<Project> {

    companion object {
        const val EXTENSION_NAME = "versioning"
        const val GROUP_TASK_NAME = "versioning"

        private const val APPLICATION_PLUGIN = "com.android.application"
        private const val DYNAMIC_FEATURE_PLUGIN = "com.android.dynamic-feature"
        private const val LIBRARY_PLUGIN = "com.android.library"
        private const val JAVA_PLATFORM_PLUGIN = "java-platform"
        private val SUPPORTED_PLUGINS =
            listOf(APPLICATION_PLUGIN, DYNAMIC_FEATURE_PLUGIN, LIBRARY_PLUGIN, JAVA_PLATFORM_PLUGIN)
    }

    override fun apply(project: Project) {
        val extension = project.extensions.create(EXTENSION_NAME, VersioningExtension::class.java)
        val versionFile = project.layout.projectDirectory.file(extension.filePath)
        val version = project.versionOf(versionFile)
        val tasks = VersioningTasks(project, extension, versionFile)

        project.pluginManager.withPlugin(APPLICATION_PLUGIN) {
            tasks.register(VersioningKind.APP)
            project.configureApplication(version)
        }
        project.pluginManager.withPlugin(DYNAMIC_FEATURE_PLUGIN) {
            // Dynamic feature modules don't own versionName/versionCode: they come from the base app.
            tasks.register(VersioningKind.APP)
        }
        project.pluginManager.withPlugin(LIBRARY_PLUGIN) {
            tasks.register(VersioningKind.LIBRARY)
            project.version = LazyProjectVersion(version.map { it.versionName })
        }
        project.pluginManager.withPlugin(JAVA_PLATFORM_PLUGIN) {
            tasks.register(VersioningKind.LIBRARY)
            project.version = LazyProjectVersion(version.map { it.versionName })
        }
        project.afterEvaluate { warnIfUnsupported() }
    }

    /**
     * Parsed version, backed by a [VersionFileValueSource] so the configuration cache is
     * invalidated whenever the version file changes.
     */
    private fun Project.versionOf(versionFile: Provider<RegularFile>): Provider<Version> =
        providers.of(VersionFileValueSource::class.java) { parameters.versionFile.set(versionFile) }
            .zip(versionFile) { content, file -> VersionFile.parse(content, file.asFile.path) }

    private fun Project.configureApplication(version: Provider<Version>) {
        val androidComponents = extensions.getByType(ApplicationAndroidComponentsExtension::class.java)
        val versionName = version.map { it.versionName }
        val versionCode = version.map { it.versionCode }
        androidComponents.onVariants { variant ->
            variant.outputs.forEach { output ->
                output.versionName.set(versionName)
                output.versionCode.set(versionCode)
            }
        }
    }

    private fun Project.warnIfUnsupported() {
        if (SUPPORTED_PLUGINS.none { pluginManager.hasPlugin(it) }) {
            logger.warn(
                "Plugin '$EXTENSION_NAME' applied to $path, but none of ${SUPPORTED_PLUGINS.joinToString()} " +
                    "is applied: versioning tasks are not registered."
            )
        }
    }
}
