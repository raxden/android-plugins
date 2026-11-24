package com.raxdenstudios.publishing

import com.raxdenstudios.checkPlugin
import com.raxdenstudios.getOrCreateExtension
import com.raxdenstudios.publishing.configurer.CoordinatesDataConfigurer
import com.raxdenstudios.publishing.configurer.PomDataConfigurer
import com.raxdenstudios.publishing.configurer.PomValidationConfigurer
import com.raxdenstudios.publishing.configurer.RepositoryDataConfigurer
import com.raxdenstudios.publishing.configurer.SigningDataConfigurer
import com.raxdenstudios.publishing.extension.PublishExtension
import com.raxdenstudios.withPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import org.gradle.plugins.signing.SigningExtension

/**
 * Shared setup of the publish plugins: applies `maven-publish` and `signing`, creates the
 * [PublishExtension] and publishes [componentName] as [publicationName] once [requiredPluginId]
 * is applied (before or after this plugin).
 *
 * Coordinates, POM, repository and signing are configured after the build script is evaluated,
 * because the APIs they feed are not lazy and must see the values set in the extension block.
 * The required-plugin check and that configuration are skipped when the project already failed
 * to evaluate, so the original error is the one reported.
 */
abstract class AbstractPublishPlugin : Plugin<Project> {

    /** ID this plugin is applied with; named in the error when [requiredPluginId] is missing. */
    protected abstract val pluginId: String

    /** Name of the `publishLibrary` / `publishPlatform` extension. */
    protected abstract val extensionName: String

    /** ID of the plugin that provides [componentName] (`com.android.library`, `java-platform`). */
    protected abstract val requiredPluginId: String

    /** Software component to publish (`release` for Android libraries, `javaPlatform` for BOMs). */
    protected abstract val componentName: String

    /**
     * Name of the [MavenPublication]. Its POM generation task is named after it, so
     * [PomValidationConfigurer] attaches to `generatePomFileFor<publicationName>Publication`.
     */
    protected abstract val publicationName: String

    /** Called once [requiredPluginId] has been applied. */
    protected open fun Project.onRequiredPluginApplied() = Unit

    override fun apply(project: Project) = with(project) {
        pluginManager.apply(MAVEN_PUBLISH_PLUGIN_ID)
        pluginManager.apply(SIGNING_PLUGIN_ID)
        getOrCreateExtension<PublishExtension>(extensionName)

        withPlugin(requiredPluginId) {
            onRequiredPluginApplied()
            registerPublication()
        }

        afterEvaluate {
            if (state.failure != null) return@afterEvaluate

            checkPlugin(plugin = requiredPluginId, requiredBy = pluginId)
            configurePublicationData()
            RepositoryDataConfigurer(project, extensionName).configure(publishing)
            SigningDataConfigurer(project, extensionName).configure(extensions.getByType<SigningExtension>())
        }
    }

    private val Project.publishing: PublishingExtension
        get() = extensions.getByType<PublishingExtension>()

    private fun Project.registerPublication() {
        components.matching { component -> component.name == componentName }.all {
            val component = this
            if (publicationName !in publishing.publications.names) {
                publishing.publications.register<MavenPublication>(publicationName) {
                    from(component)
                }
            }
        }
    }

    private fun Project.configurePublicationData() {
        val coordinatesConfigurer = CoordinatesDataConfigurer(project, extensionName)
        val pomConfigurer = PomDataConfigurer(project, extensionName)
        PomValidationConfigurer(path, publicationName, pomConfigurer.problems()).configure(tasks)
        publishing.publications.withType<MavenPublication>()
            .matching { publication -> publication.name == publicationName }
            .configureEach {
                coordinatesConfigurer.configure(this)
                pomConfigurer.configure(this)
            }
    }

    private companion object {
        const val MAVEN_PUBLISH_PLUGIN_ID = "maven-publish"
        const val SIGNING_PLUGIN_ID = "signing"
    }
}
