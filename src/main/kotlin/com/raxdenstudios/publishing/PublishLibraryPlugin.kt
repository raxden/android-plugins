package com.raxdenstudios.publishing

import com.android.build.api.dsl.LibraryExtension
import com.raxdenstudios.Plugin.MavenPublish
import com.raxdenstudios.Plugin.Signing
import com.raxdenstudios.checkLibraryPlugin
import com.raxdenstudios.getOrCreateExtension
import com.raxdenstudios.publishing.extension.PublishExtension
import com.raxdenstudios.publishing.configurer.ArtifactsConfigurer
import com.raxdenstudios.publishing.configurer.CoordinatesDataConfigurer
import com.raxdenstudios.publishing.configurer.PomDataConfigurer
import com.raxdenstudios.publishing.configurer.RepositoryDataConfigurer
import com.raxdenstudios.publishing.configurer.SigningDataConfigurer
import com.raxdenstudios.withLibraryPlugin
import com.raxdenstudios.withMavenPublishPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register
import org.gradle.plugins.signing.SigningExtension

open class PublishLibraryPlugin : Plugin<Project> {

    companion object {
        const val EXTENSION_NAME = "publishLibrary"
        private const val PUBLICATION_NAME = "release"
        private const val RELEASE_COMPONENT_NAME = "release"
    }

    override fun apply(project: Project) = with(project) {
        checkLibraryPlugin()
        initRequiredPlugins()
        initExtension()
        configure()
    }

    private fun Project.initExtension() {
        getOrCreateExtension<PublishExtension>(EXTENSION_NAME)
    }

    private fun Project.initRequiredPlugins() = plugins.apply {
        apply(MavenPublish.id)
        apply(Signing.id)
    }

    private fun Project.configure() {
        configureArtifacts()
        configurePublication()
        configureSigning()
    }

    private fun Project.configureArtifacts() {
        withLibraryPlugin {
            extensions.configure<LibraryExtension> {
                configureArtifacts(this@configureArtifacts)
            }
        }
    }

    private fun Project.configurePublication() {
        withMavenPublishPlugin {
            val publishingExtension = extensions.getByType<PublishingExtension>()
            val releaseComponents = components.matching { it.name == RELEASE_COMPONENT_NAME }

            releaseComponents.all {
                if (PUBLICATION_NAME !in publishingExtension.publications.names) {
                    publishingExtension.publications.register<MavenPublication>(PUBLICATION_NAME) {
                        from(this@all)

                        configureCoordinates(this@configurePublication)
                        configurePom(this@configurePublication)
                    }
                }
            }
            publishingExtension.configureRepositories(this@configurePublication)
        }
    }

    private fun Project.configureSigning() {
        extensions.configure<SigningExtension> {
            configureSigning(project)
        }
    }

    private fun SigningExtension.configureSigning(project: Project) {
        SigningDataConfigurer(project, EXTENSION_NAME).configure(this)
    }

    private fun MavenPublication.configureCoordinates(project: Project) {
        CoordinatesDataConfigurer(project, EXTENSION_NAME).configure(this)
    }

    private fun PublishingExtension.configureRepositories(project: Project) {
        RepositoryDataConfigurer(project, EXTENSION_NAME).configure(this)
    }

    private fun MavenPublication.configurePom(project: Project) {
        PomDataConfigurer(project, EXTENSION_NAME).configure(this)
    }

    private fun LibraryExtension.configureArtifacts(project: Project) {
        ArtifactsConfigurer(project, EXTENSION_NAME).configure(this)
    }
}
