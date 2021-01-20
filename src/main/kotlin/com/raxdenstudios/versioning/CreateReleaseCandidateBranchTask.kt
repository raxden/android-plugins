package com.raxdenstudios.versioning

import com.raxdenstudios.checkoutBranch
import org.ajoberstar.grgit.Credentials
import org.ajoberstar.grgit.Grgit
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class CreateReleaseCandidateBranchTask : DefaultTask() {

  private val appVersionProvider: AppVersionProvider by lazy { AppVersionProvider(project) }
  private val appVersionCredentialsProvider: AppVersionCredentialsProvider by lazy {
    AppVersionCredentialsProvider(project)
  }
  private val masterBranch = "master"
  private val releaseBranch by lazy { "releases/release-${appVersionProvider.major}.${appVersionProvider.minor}" }
  private val commitMessage by lazy { "Bump version to ${appVersionProvider.versionName}" }

  private val user: String by lazy { appVersionCredentialsProvider.user }
  private val password: String by lazy { appVersionCredentialsProvider.password }

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
    appVersionProvider.increaseMinorVersion()
    appVersionProvider.resetPatchVersion()
    add { patterns = mutableSetOf(".") }
    commit { message = commitMessage }
    push()
  }
}


