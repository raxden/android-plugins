package com.raxdenstudios.publishing.provider

import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension

class RepositoryDataProvider(
  private val project: Project
) : DataProvider(project) {

  private val repository = extension.repository
  private val credentials = repository.credentials
  private val name: String = repository.name
  private val url: String = repository.url
  private val username: String
    get() {
      return when {
        credentials.username.isNotEmpty() -> credentials.username
        project.properties["OSSRH_USERNAME"] != null -> project.properties["OSSRH_USERNAME"] as String
        System.getenv("OSSRH_USERNAME") != null -> System.getenv("OSSRH_USERNAME")
        else -> ""
      }
    }
  private val password: String
    get() {
      return when {
        credentials.password.isNotEmpty() -> credentials.password
        project.properties["OSSRH_PASSWORD"] != null -> project.properties["OSSRH_PASSWORD"] as String
        System.getenv("OSSRH_PASSWORD") != null -> System.getenv("OSSRH_PASSWORD")
        else -> ""
      }
    }

  fun configure(extension: PublishingExtension) {
    extension.run {
      repositories {
        maven {
          name = this@RepositoryDataProvider.name
          url = project.uri(this@RepositoryDataProvider.url)
          credentials {
            username = this@RepositoryDataProvider.username
            password = this@RepositoryDataProvider.password
          }
        }
      }
    }
  }
}
