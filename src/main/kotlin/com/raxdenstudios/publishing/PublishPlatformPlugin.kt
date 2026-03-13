package com.raxdenstudios.publishing

import com.raxdenstudios.Plugin.MavenPublish
import com.raxdenstudios.Plugin.Signing
import com.raxdenstudios.checkPlatformPlugin
import com.raxdenstudios.getOrCreateExtension
import com.raxdenstudios.publishing.configurer.CoordinatesDataConfigurer
import com.raxdenstudios.publishing.configurer.PomDataConfigurer
import com.raxdenstudios.publishing.configurer.RepositoryDataConfigurer
import com.raxdenstudios.publishing.configurer.SigningDataConfigurer
import com.raxdenstudios.publishing.extension.PublishExtension
import com.raxdenstudios.withJavaPlatformPlugin
import com.raxdenstudios.withMavenPublishPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register
import org.gradle.plugins.signing.SigningExtension

open class PublishPlatformPlugin : Plugin<Project> {

    companion object {
        const val EXTENSION_NAME = "publishPlatform"
        private const val PUBLICATION_NAME = "platform"
        private const val JAVA_PLATFORM_COMPONENT_NAME = "javaPlatform"
    }

    override fun apply(project: Project) = with(project) {
        checkPlatformPlugin()
        initRequiredPlugins()
        initExtension()
        configure()
    }

    private fun Project.initRequiredPlugins() {
        plugins.apply(MavenPublish.id)
        plugins.apply(Signing.id)
    }

    private fun Project.initExtension() {
        getOrCreateExtension<PublishExtension>(EXTENSION_NAME)
    }

    private fun Project.configure() {
        configurePublication()
        configureSigning()
    }

    private fun Project.configurePublication() {
        withMavenPublishPlugin {
            withJavaPlatformPlugin {
                afterEvaluate {
                    val publishing = extensions.getByType<PublishingExtension>()

                    if (PUBLICATION_NAME !in publishing.publications.names) {
                        publishing.publications.register<MavenPublication>(PUBLICATION_NAME) {
                            from(components.getByName(JAVA_PLATFORM_COMPONENT_NAME))

                            configureCoordinates(project)
                            configurePom(project)
                        }
                    }

                    publishing.configureRepositories(project)
                }
            }
        }
    }

    private fun Project.configureSigning() {
        extensions.configure<SigningExtension> {
            configureSigningExtension(project)
        }
    }

    private fun SigningExtension.configureSigningExtension(project: Project) {
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
}