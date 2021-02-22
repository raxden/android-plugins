package com.raxdenstudios.publishing.provider

import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPublication

class CoordinatesDataProvider(
  private val project: Project
) : DataProvider(project) {

  private val coordinates = extension.coordinates

  private val groupId: String
    get() = if (coordinates.groupId.isNotEmpty()) coordinates.groupId
    else project.group.toString()

  private val artifactId: String
    get() = if (coordinates.artifactId.isNotEmpty()) coordinates.artifactId
    else project.name

  private val version: String
    get() = if (coordinates.version.isNotEmpty()) extension.coordinates.version
    else project.version.toString()

  fun configure(publication: MavenPublication) {
    publication.run {
      groupId = this@CoordinatesDataProvider.groupId
      artifactId = this@CoordinatesDataProvider.artifactId
      version = this@CoordinatesDataProvider.version
    }
  }
}
