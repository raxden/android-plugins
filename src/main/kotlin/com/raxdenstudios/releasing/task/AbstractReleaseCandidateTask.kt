package com.raxdenstudios.releasing.task

import com.raxdenstudios.releasing.extension.ReleasingExtension
import com.raxdenstudios.releasing.provider.CredentialsProvider
import com.raxdenstudios.versioning.provider.FileVersionProvider
import org.ajoberstar.grgit.Credentials
import org.ajoberstar.grgit.Grgit
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Internal

abstract class AbstractReleaseCandidateTask : DefaultTask() {

  companion object {
    const val EXTENSION_NAME = "releasing"
    const val MASTER_BRANCH = "master"
  }

  private val extension: ReleasingExtension by lazy {
    project.extensions.getByName(EXTENSION_NAME) as ReleasingExtension
  }
  private val credentialsProvider: CredentialsProvider by lazy {
    CredentialsProvider(extension.credentials)
  }
  private val fileVersionProvider: FileVersionProvider by lazy {
    FileVersionProvider(extension.versionFilePath)
  }
  private val user: String by lazy { credentialsProvider.user }
  private val password: String by lazy { credentialsProvider.password }

  fun openGitWithCredentials(): Grgit = Grgit.open {
    credentials = Credentials(user, password)
    currentDir = project.rootDir
  }

  fun increaseMinorVersion() {
    fileVersionProvider.increaseMinorVersion()
  }

  fun resetPatchVersion() {
    fileVersionProvider.resetPatchVersion()
  }

  fun increasePatchVersion() {
    fileVersionProvider.increasePatchVersion()
  }

  @Internal
  fun getReleaseBranch() = "release/${fileVersionProvider.major}.${fileVersionProvider.minor}"

  @Internal
  fun getTagName() = fileVersionProvider.versionName

  @Internal
  fun getCommitBumpVersionMessage() = "Bump version to ${fileVersionProvider.versionName}"
}
