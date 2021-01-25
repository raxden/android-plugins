package com.raxdenstudios.publish.provider

import com.raxdenstudios.publish.extension.PublishLibraryExtension
import com.raxdenstudios.publish.model.Repository
import org.gradle.api.Project

class RepositoryBintrayDataProvider(
  private val project: Project
) {

  private val extension: PublishLibraryExtension by lazy {
    project.extensions.getByName("publishLibrary") as PublishLibraryExtension
  }

  val name: String
    get() = extension.name

  val user: String
    get() = extension.username

  val description: String
    get() = extension.description

  val vcsUrl: String
    get() = "${extension.web}.git"

  val websiteUrl: String
    get() = extension.web

  val issueTrackerUrl: String
    get() = "${extension.web}/issues"

  val repo: String
    get() = when (val credentials = extension.repository) {
      is Repository.Bintray -> credentials.name
    }

  val key: String
    get() = when (val credentials = extension.repository) {
      is Repository.Bintray -> credentials.key
    }

  val publish: Boolean
    get() = when (val credentials = extension.repository) {
      is Repository.Bintray -> credentials.publish
    }

  val override: Boolean
    get() = when (val credentials = extension.repository) {
      is Repository.Bintray -> credentials.override
    }
}
