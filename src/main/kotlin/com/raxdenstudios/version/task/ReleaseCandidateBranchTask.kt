package com.raxdenstudios.version.task

import com.raxdenstudios.checkoutBranch
import com.raxdenstudios.version.provider.CredentialsProvider
import com.raxdenstudios.version.provider.FileVersionProvider
import org.ajoberstar.grgit.Credentials
import org.ajoberstar.grgit.Grgit
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class ReleaseCandidateBranchTask : DefaultTask() {

  private val fileVersionProvider: FileVersionProvider by lazy { FileVersionProvider(project) }
  private val credentialsProvider: CredentialsProvider by lazy { CredentialsProvider(project) }
  private val masterBranch = "master"
  private val releaseBranch by lazy { "releases/release-${fileVersionProvider.major}.${fileVersionProvider.minor}" }
  private val commitMessage by lazy { "Bump version to ${fileVersionProvider.versionName}" }

  private val user: String by lazy { credentialsProvider.user }
  private val password: String by lazy { credentialsProvider.password }

  @TaskAction
  fun execute() {
    Grgit.open { credentials = Credentials(user, password) }.run {
      checkoutBranch(masterBranch)
      bumpVersion()
      createReleaseCandidateBranch()
      close()
    }
  }

  private fun Grgit.createReleaseCandidateBranch() = push {
    remote = "origin"
    refsOrSpecs = listOf("HEAD:refs/heads/$releaseBranch")
  }

  private fun Grgit.bumpVersion() {
    fileVersionProvider.increaseMinorVersion()
    fileVersionProvider.resetPatchVersion()
    add { patterns = mutableSetOf(".") }
    commit { message = commitMessage }
    push()
  }
}


