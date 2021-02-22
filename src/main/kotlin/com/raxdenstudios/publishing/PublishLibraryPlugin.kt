package com.raxdenstudios.publishing

import com.raxdenstudios.publishing.provider.PomDataProvider
import com.raxdenstudios.publishing.provider.CoordinatesDataProvider
import com.raxdenstudios.publishing.provider.ArtifactsProvider
import com.raxdenstudios.publishing.provider.RepositoryDataProvider
import com.raxdenstudios.publishing.provider.SigningDataProvider
import com.raxdenstudios.publishing.extension.PublishLibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.get
import org.gradle.plugins.signing.SigningExtension

/*
* ./gradlew publishToMavenLocal
* ./gradlew clean publishReleasePublicationToSonatypeRepository
*/
open class PublishLibraryPlugin : Plugin<Project> {

  private lateinit var extension: PublishLibraryExtension

  override fun apply(project: Project) {
    extension = project.extensions.create("publishLibrary")

    project.configurePlugins()
    project.configurePublishing()
    project.configureSigning()
  }

  private fun Project.configurePlugins() {
    plugins.apply("org.gradle.maven-publish")
    plugins.apply("org.gradle.signing")
  }

  private fun Project.configurePublishing() {
    publishing {
      // Because the components are created only during the afterEvaluate phase, you must
      // configure your publications using the afterEvaluate() lifecycle method.
      afterEvaluate {
        publications {
          // Creates a Maven publication called "release".
          create<MavenPublication>("release") {
            // Applies the component for the release build variant.
            from(components["release"])

            configureCoordinates(project)
            configureArtifacts(project)
            configurePom(project)
            configureRepositories(project)
          }
        }
      }
    }
  }

  @Suppress("UnstableApiUsage")
  private fun Project.configureSigning() {
    signing {
      val provider = SigningDataProvider(project)
      provider.configure(this)
    }
  }

  private fun MavenPublication.configureCoordinates(project: Project) {
    val provider = CoordinatesDataProvider(project)
    provider.configure(this)
  }

  private fun MavenPublication.configureArtifacts(project: Project) {
    val provider = ArtifactsProvider(project)
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

  private fun Project.publishing(configure: PublishingExtension.() -> Unit): Unit =
    (this as ExtensionAware).extensions.configure("publishing", configure)

  private fun Project.signing(configure: SigningExtension.() -> Unit): Unit =
    (this as ExtensionAware).extensions.configure("signing", configure)
}
