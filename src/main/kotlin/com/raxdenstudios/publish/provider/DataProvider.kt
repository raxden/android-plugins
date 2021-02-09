package com.raxdenstudios.publish.provider

import com.raxdenstudios.publish.extension.PublishLibraryExtension
import org.gradle.api.Project

abstract class DataProvider(
  private val project: Project
) {

  protected val extension: PublishLibraryExtension by lazy {
    project.extensions.getByName("publishLibrary") as PublishLibraryExtension
  }
}
