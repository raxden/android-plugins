package com.raxdenstudios.publish

import com.android.build.gradle.LibraryExtension
import com.jfrog.bintray.gradle.BintrayExtension
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
open class PublishJCenterPlugin : Plugin<Project> {

  private lateinit var extension: PublishMavenCentralExtension

  override fun apply(project: Project) {
    extension = project.extensions.create("publishJCenter")

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
        // Creates a Maven publication called "release".
        publications {
          create<MavenPublication>("release") {
            // Applies the component for the release build variant.
            from(components["release"])

            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()

            configureArtifacts(this@configurePublish)
            configurePom()
          }
        }
      }
    }
  }

  private fun Project.configureBintray() {
    bintray {
      afterEvaluate {
        user = extension.username
        key = extension.bintray.key
        publish = true
        setPublications("release")
        override = true

        pkg.apply {
          repo = extension.bintray.repository
          name = extension.name
          description = extension.description
          desc = description
          publicDownloadNumbers = true
          setLicenses("Apache-2.0")
          vcsUrl = "${extension.website}.git"
          websiteUrl = extension.website
          issueTrackerUrl = "${extension.website}/issues"
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

  private fun MavenPublication.configurePom() {
    pom {
      name.set(extension.name)
      description.set(extension.description)
      url.set(extension.website)
      licenses {
        license {
          name.set("The Apache License, Version 2.0")
          url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
        }
      }
      developers {
        developer {
          id.set(extension.username)
          name.set(extension.developerName)
          email.set(extension.email)
        }
      }
      scm {
        val urlWithoutSchema = extension.website.removePrefix("https://")
        connection.set("scm:git:$urlWithoutSchema.git")
        developerConnection.set("scm:git:ssh://$urlWithoutSchema.git")
        url.set("${extension.website}/tree/master")
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
