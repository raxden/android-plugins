package com.raxdenstudios.versioning.provider

import com.raxdenstudios.versioning.extension.VersioningExtension
import com.raxdenstudios.versioning.model.GitCredentials
import org.gradle.api.Project

class CredentialsProvider(
  private val project: Project
) {

  private val extension: VersioningExtension by lazy {
    project.extensions.getByName("versioning") as VersioningExtension
  }
  private val credentials: GitCredentials by lazy { extension.credentials }

  val user: String
    get() = credentials.user
  val password: String
    get() = credentials.password
}
