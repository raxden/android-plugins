package com.raxdenstudios.publish

import com.raxdenstudios.publish.provider.PomDataProvider
import com.raxdenstudios.publish.provider.RepositoryBintrayDataProvider
import com.android.build.gradle.LibraryExtension
import com.jfrog.bintray.gradle.BintrayExtension
import com.raxdenstudios.publish.extension.PublishLibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.register
import java.util.*

/*
* ./gradlew publishToMavenLocal
* ./gradlew clean publish bintrayUpload --info
 */
open class PublishLibraryPlugin : Plugin<Project> {

  private lateinit var extension: PublishLibraryExtension

  override fun apply(project: Project) {
    extension = project.extensions.create("publishLibrary")

    project.configurePlugins()
    project.configurePublish()
    project.configureBintray()
  }

  private fun Project.configurePlugins() {
    plugins.apply("org.gradle.maven-publish")
    plugins.apply("com.jfrog.bintray")
  }

  private fun Project.configurePublish() {
    publishing {
      // Because the components are created only during the afterEvaluate phase, you must
      // configure your publications using the afterEvaluate() lifecycle method.
      afterEvaluate {
        publications {
          // Creates a Maven publication called "release".
          create<MavenPublication>("release") {
            // Applies the component for the release build variant.
            from(components["release"])

            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()

            configureArtifacts(this@configurePublish)
            configurePom(project)
          }
        }
      }
    }
  }

  private fun Project.configureBintray() {
    val provider = RepositoryBintrayDataProvider(this)
    afterEvaluate {
      bintray {
        user = provider.user
        key = provider.key
        publish = provider.publish
        setPublications("release")
        override = provider.override

        pkg.apply {
          repo = provider.repo
          name = provider.name
          description = provider.description
          desc = description
          publicDownloadNumbers = true
          setLicenses("Apache-2.0")
          vcsUrl = provider.vcsUrl
          websiteUrl = provider.websiteUrl
          issueTrackerUrl = provider.issueTrackerUrl
          version.apply {
            name = project.version.toString()
            desc = "Version ${project.version}"
            released = Date().toString()
            vcsTag = project.version.toString()
          }
        }
      }
    }
  }

  private fun MavenPublication.configureArtifacts(project: Project) {
    artifact(project.getAndroidSourcesJar())
  }

  private fun MavenPublication.configurePom(project: Project) {
    val provider = PomDataProvider(project)
    pom {
      name.set(provider.name)
      description.set(provider.description)
      url.set(provider.url)
      licenses {
        license {
          name.set(provider.licenseName)
          url.set(provider.licenseUrl)
        }
      }
      developers {
        developer {
          id.set(provider.developerId)
          name.set(provider.developerName)
          email.set(provider.developerEmail)
        }
      }
      scm {
        connection.set(provider.scmConnection)
        developerConnection.set(provider.scmDeveloperConnection)
        url.set(provider.scmUrl)
      }
    }
  }

  private fun Project.getAndroidSourcesJar(): Jar {
    return tasks.register<Jar>("androidSourcesJar") {
      archiveClassifier.set("sources")
      from((project.extensions.findByName("android") as LibraryExtension).sourceSets["main"].java.srcDirs)
    }.get()
  }

  private fun Project.publishing(configure: PublishingExtension.() -> Unit): Unit =
    (this as ExtensionAware).extensions.configure("publishing", configure)

  private fun Project.bintray(configure: BintrayExtension.() -> Unit): Unit =
    (this as ExtensionAware).extensions.configure("bintray", configure)
}
