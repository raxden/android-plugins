package com.raxdenstudios.versioning

import org.gradle.api.Project

class AppVersionCredentialsProvider(
  private val project: Project
) {

  private val extension: AppVersionExtension by lazy {
    project.extensions.getByName("versioning") as AppVersionExtension
  }
  private val credentials: Credentials by lazy { extension.credentials }

  val user: String
    get() = credentials.user
  val password: String
    get() = credentials.password
}
