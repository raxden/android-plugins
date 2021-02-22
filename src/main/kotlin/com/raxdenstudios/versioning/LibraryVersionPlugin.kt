package com.raxdenstudios.versioning

import com.android.build.gradle.BaseExtension
import com.raxdenstudios.versioning.extension.LibraryVersionExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.getByType

open class LibraryVersionPlugin : VersionPlugin<LibraryVersionExtension>() {

  override fun Project.createExtension(): LibraryVersionExtension =
    extensions.create("versioning")

  override fun Project.configureProject() {
    pluginManager.withPlugin("com.android.library") { configure() }
  }

  private fun Project.configure() {
    extensions.getByType<BaseExtension>().run {
      version = fileVersionProvider.versionName
      group = extension.group
    }
  }
}
