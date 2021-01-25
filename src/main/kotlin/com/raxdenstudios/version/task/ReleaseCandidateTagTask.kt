package com.raxdenstudios.version.task

import com.raxdenstudios.checkoutBranch
import com.raxdenstudios.version.provider.CredentialsProvider
import com.raxdenstudios.version.provider.FileVersionProvider
import org.ajoberstar.grgit.Credentials
import org.ajoberstar.grgit.Grgit
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class ReleaseCandidateTagTask : DefaultTask() {

  private val fileVersionProvider: FileVersionProvider by lazy { FileVersionProvider(project) }
  private val credentialsProvider: CredentialsProvider by lazy { CredentialsProvider(project) }
  private val masterBranch = "master"
  private val releaseBranch by lazy { "releases/release-${fileVersionProvider.major}.${fileVersionProvider.minor}" }
  private val commitMessage by lazy { "Bump version to ${fileVersionProvider.versionName}" }
  private val tagName by lazy { fileVersionProvider.versionName }

  private val user: String by lazy { credentialsProvider.user }
  private val password: String by lazy { credentialsProvider.password }

  @TaskAction
  fun execute() {
    Grgit.open { credentials = Credentials(user, password) }.run {
      checkoutBranch(releaseBranch)
      createTagRelease()
      bumpVersion()
      checkoutBranch(masterBranch)
      close()
    }
  }

  private fun Grgit.createTagRelease() {
    tag.add { name = tagName }
    push { tags = true }
  }

  private fun Grgit.bumpVersion() {
    fileVersionProvider.increasePatchVersion()
    add { patterns = mutableSetOf(".") }
    commit { message = commitMessage }
    push()
  }
}
