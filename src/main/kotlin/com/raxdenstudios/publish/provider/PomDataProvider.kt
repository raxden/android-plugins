package com.raxdenstudios.publish.provider

import com.raxdenstudios.publish.extension.PublishLibraryExtension
import org.gradle.api.Project

open class PomDataProvider(
  private val project: Project
) {

  private val extension: PublishLibraryExtension by lazy {
    project.extensions.getByName("publishLibrary") as PublishLibraryExtension
  }

  val name: String
    get() = extension.name

  val description: String
    get() = extension.description

  val url: String
    get() = extension.web

  val licenseName: String
    get() = "The Apache License, Version 2.0"

  val licenseUrl: String
    get() = "http://www.apache.org/licenses/LICENSE-2.0.txt"

  val developerId: String
    get() = extension.username

  val developerName: String
    get() = extension.developerName

  val developerEmail: String
    get() = extension.developerEmail

  val scmConnection: String
    get() {
      val urlWithoutSchema = extension.web.removePrefix("https://")
      return "scm:git:$urlWithoutSchema.git"
    }

  val scmDeveloperConnection: String
    get() {
      val urlWithoutSchema = extension.web.removePrefix("https://")
      return "scm:git:ssh://$urlWithoutSchema.git"
    }

  val scmUrl: String
    get() = "${extension.web}/tree/master"
}
