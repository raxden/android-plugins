package com.raxdenstudios.publish

import com.android.build.gradle.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register
import org.gradle.plugins.signing.SigningExtension

open class PublishJitPackPlugin : Plugin<Project> {

  private val Project.publishing: PublishingExtension
    get() = (this as ExtensionAware).extensions.getByName("publishing") as PublishingExtension

  private lateinit var extension: PublishMavenCentralExtension

  override fun apply(project: Project) {
    extension = project.extensions.create("publishMavenCentral")

    project.configurePlugins()
    project.configurePublish()
    project.configureSigning()
  }

  private fun Project.configurePlugins() {
    plugins.apply("org.gradle.maven-publish")
    plugins.apply("org.gradle.signing")
  }

  private fun Project.configurePublish() {
    publishing {
      afterEvaluate {
        publications {
          create<MavenPublication>("release") {

            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()

            // Two artifacts, the `aar` and the sources
            configureArtifacts(this@configurePublish)
            configurePom(this@configurePublish)
          }
        }
        repositories {
          maven {
            name = "sonatype"

            val releasesRepoUrl = "https://oss.sonatype.org/service/local/staging/deploy/maven2/"
            val snapshotsRepoUrl = "https://oss.sonatype.org/content/repositories/snapshots/"

            // change to point to your repo, e.g. http://my.org/repo
//            url = uri("$buildDir/repo")
            val currentVersion = version as String
            val repositoryToDeploy =
              if (currentVersion.endsWith("SNAPSHOT")) uri(snapshotsRepoUrl)
              else uri(releasesRepoUrl)
            url = uri(repositoryToDeploy)

            credentials {
              username = System.getenv("OSSRH_USERNAME") ?: project.properties["OSSRH_USERNAME"] as String
              password = System.getenv("OSSRH_PASSWORD") ?: project.properties["OSSRH_PASSWORD"] as String
            }
          }
        }
      }
    }
  }

  private fun MavenPublication.configureArtifacts(project: Project) {
    artifact(project.getAndroidSourcesJar())
    artifact("${project.buildDir}/outputs/aar/${project.name}-release.aar")
  }

  private fun Project.configureSigning() {
    signing {
      val signingKeyId = System.getenv("SIGNING_KEY_ID") ?: project.properties["SIGNING_KEY_ID"] as String
      val signingKey = System.getenv("SIGNING_KEY") ?: project.properties["SIGNING_KEY"] as String
      val signingPassword = System.getenv("SIGNING_PASSWORD") ?: project.properties["SIGNING_PASSWORD"] as String
      useInMemoryPgpKeys(signingKeyId, signingKey, signingPassword)
      sign(publishing.publications)
    }
  }

  private fun MavenPublication.configurePom(project: Project) {
    pom {
      name.set(extension.pomName)
      description.set(extension.pomDescription)
      url.set(extension.pomUrl)
      licenses {
        license {
          name.set("The Apache License, Version 2.0")
          url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
        }
      }
      developers {
        developer {
          id.set(extension.pomDeveloperId)
          name.set(extension.pomDeveloperName)
          email.set(extension.pomDeveloperEmail)
        }
      }

      scm {
        connection.set(extension.pomScmConnection)
        developerConnection.set(extension.pomScmDeveloperConnection)
        url.set(extension.pomScmUrl)
      }

      withXml {
        val dependenciesNode = asNode().appendNode("dependencies")
        val implementation = project.configurations.named<Configuration>("implementation")
        implementation.get().allDependencies.forEach {
          val dependencyNode = dependenciesNode.appendNode("dependency")
          dependencyNode.appendNode("groupId", it.group)
          dependencyNode.appendNode("artifactId", it.name)
          dependencyNode.appendNode("version", it.version)
        }
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

  private fun Project.signing(configure: SigningExtension.() -> Unit): Unit =
    (this as ExtensionAware).extensions.configure("signing", configure)
}
