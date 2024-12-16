package com.raxdenstudios.publishing

import com.raxdenstudios.publishing.extension.PublishLibraryExtension
import com.raxdenstudios.publishing.provider.CoordinatesDataProvider
import com.raxdenstudios.publishing.provider.PomDataProvider
import com.raxdenstudios.publishing.provider.RepositoryDataProvider
import com.raxdenstudios.publishing.provider.SigningDataProvider
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.get
import com.raxdenstudios.Plugin.MavenPublish
import com.raxdenstudios.Plugin.Signing
import com.raxdenstudios.checkLibraryPlugin
import com.raxdenstudios.publishing
import com.raxdenstudios.signing

/*
* ./gradlew publishToMavenLocal
* ./gradlew publishToSonatype closeAndReleaseSonatypeStagingRepository
*/
open class PublishLibraryPlugin : Plugin<Project> {

    companion object {
        const val EXTENSION_NAME = "publishLibrary"
    }

    private lateinit var extension: PublishLibraryExtension

    override fun apply(project: Project) = with(project) {
        checkLibraryPlugin()
        initPublishLibraryExtension()
        initRequiredPlugins()
        configurePublishingExtension()
        configureSigningExtension()
    }

    private fun Project.initPublishLibraryExtension() {
        extension = PublishLibraryExtension.create(this)
    }

    private fun Project.initRequiredPlugins() = plugins.apply {
        apply(MavenPublish.id)
        apply(Signing.id)
    }

    private fun Project.configurePublishingExtension() = publishing {
        // Because the components are created only during the afterEvaluate phase, you must
        // configure your publications using the afterEvaluate() lifecycle method.
        afterEvaluate {
            publications {
                // Creates a Maven publication called "release".
                create<MavenPublication>("release") {
                    // Applies the component for the release build variant.
                    from(components["release"])

                    configureCoordinates(project)
                    configurePom(project)
                    configureRepositories(project)
                }
            }
        }
    }

    private fun Project.configureSigningExtension() = signing {
        val provider = SigningDataProvider(project)
        provider.configure(this)
    }

    private fun MavenPublication.configureCoordinates(project: Project) {
        val provider = CoordinatesDataProvider(project)
        provider.configure(this)
    }

    private fun PublishingExtension.configureRepositories(project: Project) {
        val provider = RepositoryDataProvider(project)
        provider.configure(this)
    }

    private fun MavenPublication.configurePom(project: Project) {
        val provider = PomDataProvider(project)
        provider.configure(this)
    }
}
