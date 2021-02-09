package com.raxdenstudios.version.task

import com.raxdenstudios.version.provider.FileVersionProvider
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

open class FileVersionProviderTask : DefaultTask() {

  private val fileVersionProvider: FileVersionProvider by lazy { FileVersionProvider(project) }

  @get:Input
  val major: Int
    get() = fileVersionProvider.major
  @get:Input
  val minor: Int
    get() = fileVersionProvider.minor
  @get:Input
  val patch: Int
    get() = fileVersionProvider.patch

  @get:Input
  val dev: Int
    get() = fileVersionProvider.dev
  @get:Input
  val versionCode: Int
    get() = fileVersionProvider.versionCode
  @get:Input
  val versionName: String
    get() = fileVersionProvider.versionName
}
