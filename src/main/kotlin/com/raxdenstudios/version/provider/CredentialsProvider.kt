package com.raxdenstudios.version.provider

import com.raxdenstudios.version.extension.VersionExtension
import com.raxdenstudios.version.model.Credentials
import org.gradle.api.Project

class CredentialsProvider(
  private val project: Project
) {

  private val extension: VersionExtension by lazy {
    project.extensions.getByName("versioning") as VersionExtension
  }
  private val credentials: Credentials by lazy { extension.credentials }

  val user: String
    get() = credentials.user
  val password: String
    get() = credentials.password
}
