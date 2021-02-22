package com.raxdenstudios.publishing.provider

import com.raxdenstudios.publishing.extension.PublishLibraryExtension
import org.gradle.api.Project

abstract class DataProvider(
  private val project: Project
) {

  protected val extension: PublishLibraryExtension by lazy {
    project.extensions.getByName("publishLibrary") as PublishLibraryExtension
  }
}
