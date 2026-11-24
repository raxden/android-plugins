package com.raxdenstudios.publishing

/**
 * Publishes a Java Platform / BOM module (`java-platform`) to Maven as the `platform` publication.
 * `java-platform` must be applied to the same project (plugin order does not matter); this plugin
 * does not apply it for you.
 *
 * Configure the publication with the `publishPlatform` extension (see [PublishExtension]).
 *
 * ## Setup
 *
 * ```kotlin
 * // build.gradle.kts of the BOM module
 * plugins {
 *     id("java-platform")
 *     id("com.raxdenstudios.android-publish-platform")
 * }
 * ```
 *
 * ## Usage
 *
 * ```kotlin
 * import com.raxdenstudios.publishing.model.Coordinates
 * import com.raxdenstudios.publishing.model.Developer
 *
 * group = "com.raxdenstudios"
 * version = "1.0.0"
 *
 * publishPlatform {
 *     name = "My BOM"
 *     description = "Versions of the library modules"
 *     url = "https://github.com/owner/repo"
 *     developer = Developer(id = "owner", name = "Owner", email = "owner@example.com")
 *     coordinates = Coordinates.default.copy(artifactId = "my-bom")
 * }
 * ```
 *
 * `./gradlew publishToMavenLocal` works without signing keys. Publishing to a remote repository
 * requires signing keys (see [SigningDataConfigurer]).
 */
open class PublishPlatformPlugin : AbstractPublishPlugin() {

    companion object {
        const val EXTENSION_NAME = "publishPlatform"
        private const val PLUGIN_ID = "com.raxdenstudios.android-publish-platform"
        private const val JAVA_PLATFORM_PLUGIN_ID = "java-platform"
        private const val PUBLICATION_NAME = "platform"
        private const val JAVA_PLATFORM_COMPONENT_NAME = "javaPlatform"
    }

    override val pluginId = PLUGIN_ID
    override val extensionName = EXTENSION_NAME
    override val requiredPluginId = JAVA_PLATFORM_PLUGIN_ID
    override val componentName = JAVA_PLATFORM_COMPONENT_NAME
    override val publicationName = PUBLICATION_NAME
}
