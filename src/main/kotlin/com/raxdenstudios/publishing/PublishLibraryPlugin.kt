package com.raxdenstudios.publishing

import com.android.build.api.dsl.LibraryExtension
import com.raxdenstudios.publishing.configurer.ArtifactsConfigurer
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

/**
 * Publishes the `release` variant of an Android library (`com.android.library`) to Maven, with
 * sources and javadoc jars. Plugin order does not matter; `com.android.library` must be applied
 * to the same project, otherwise evaluation fails.
 *
 * Configure the publication with the `publishLibrary` extension (see [PublishExtension]).
 *
 * ## Setup
 *
 * ```kotlin
 * // build.gradle.kts of the library module
 * plugins {
 *     id("com.android.library")
 *     id("com.raxdenstudios.android-publish-library")
 * }
 * ```
 *
 * ## Usage
 *
 * ```kotlin
 * import com.raxdenstudios.publishing.model.Developer
 *
 * group = "com.raxdenstudios"
 * version = "1.0.0"
 *
 * publishLibrary {
 *     name = "My Library"
 *     description = "Short description of the library"
 *     url = "https://github.com/owner/repo"
 *     developer = Developer(id = "owner", name = "Owner", email = "owner@example.com")
 * }
 * ```
 *
 * `./gradlew publishToMavenLocal` works without signing keys. Publishing to a remote repository
 * requires signing keys (see [SigningDataConfigurer]).
 */
open class PublishLibraryPlugin : AbstractPublishPlugin() {

    companion object {
        const val EXTENSION_NAME = "publishLibrary"
        private const val PLUGIN_ID = "com.raxdenstudios.android-publish-library"
        private const val ANDROID_LIBRARY_PLUGIN_ID = "com.android.library"
        private const val PUBLICATION_NAME = "release"
        private const val RELEASE_COMPONENT_NAME = "release"
    }

    override val pluginId = PLUGIN_ID
    override val extensionName = EXTENSION_NAME
    override val requiredPluginId = ANDROID_LIBRARY_PLUGIN_ID
    override val componentName = RELEASE_COMPONENT_NAME
    override val publicationName = PUBLICATION_NAME

    override fun Project.onRequiredPluginApplied() {
        extensions.configure<LibraryExtension> {
            ArtifactsConfigurer().configure(this)
        }
    }
}
