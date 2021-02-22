package com.raxdenstudios.publishing.provider

import com.raxdenstudios.publishing.model.Developer
import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPublication

class PomDataProvider(
  project: Project
) : DataProvider(project) {

  private val pom = extension.pom
  private val name: String
    get() = when {
      pom.name.isNotEmpty() -> pom.name
      else -> extension.name
    }
  private val description
    get() = when {
      pom.description.isNotEmpty() -> pom.description
      else -> extension.description
    }
  private val url
    get() = when {
      pom.url.isNotEmpty() -> pom.url
      else -> extension.url
    }
  private val licenses = pom.licenses
  private val developers
    get() = when {
      pom.developers.isNotEmpty() -> pom.developers
      else -> listOf(
        Developer(extension.developerId, extension.developerName, extension.developerEmail)
      )
    }
  private val scmConnection: String
    get() = when {
      pom.scm.connection.isNotEmpty() -> pom.scm.connection
      else -> "scm:git:${url.removePrefix("https://")}.git"
    }
  private val scmDeveloperConnection: String
    get() = when {
      pom.scm.developerConnection.isNotEmpty() -> pom.scm.developerConnection
      else -> "scm:git:ssh://${url.removePrefix("https://")}.git"
    }
  private val scmUrl: String
    get() = when {
      pom.scm.url.isNotEmpty() -> pom.scm.url
      else -> "$url/tree/master"
    }

  fun configure(publication: MavenPublication) {
    publication.run {
      pom {
        name.set(this@PomDataProvider.name)
        description.set(this@PomDataProvider.description)
        url.set(this@PomDataProvider.url)
        licenses {
          this@PomDataProvider.licenses.forEach { license ->
            license {
              name.set(license.name)
              url.set(license.url)
            }
          }
        }
        developers {
          this@PomDataProvider.developers.forEach { developer ->
            developer {
              id.set(developer.id)
              name.set(developer.name)
              email.set(developer.email)
            }
          }
        }
        scm {
          connection.set(this@PomDataProvider.scmConnection)
          developerConnection.set(this@PomDataProvider.scmDeveloperConnection)
          url.set(this@PomDataProvider.scmUrl)
        }
//      withXml {
//        val dependenciesNode = asNode().appendNode("dependencies")
//        val implementation = project.configurations.named<Configuration>("implementation")
//        implementation.get().allDependencies.forEach {
//          val dependencyNode = dependenciesNode.appendNode("dependency")
//          dependencyNode.appendNode("groupId", it.group)
//          dependencyNode.appendNode("artifactId", it.name)
//          dependencyNode.appendNode("version", it.version)
//        }
//      }
      }
    }
  }
}
