package com.raxdenstudios.versioning

import com.raxdenstudios.checkoutBranch
import org.ajoberstar.grgit.Credentials
import org.ajoberstar.grgit.Grgit
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class ReleaseCandidateTagTask : DefaultTask() {

  private val appVersionProvider: AppVersionProvider by lazy { AppVersionProvider(project) }
  private val appVersionCredentialsProvider: AppVersionCredentialsProvider by lazy {
    AppVersionCredentialsProvider(project)
  }
  private val releaseBranch by lazy { "releases/release-${appVersionProvider.major}.${appVersionProvider.minor}" }
  private val commitMessage by lazy { "Bump version to ${appVersionProvider.versionName}" }
  private val tagName by lazy { appVersionProvider.versionName }

  private val user: String by lazy { appVersionCredentialsProvider.user }
  private val password: String by lazy { appVersionCredentialsProvider.password }

  @TaskAction
  fun execute() {
    Grgit.open { credentials = Credentials(user, password) }.run {
      checkoutBranch(releaseBranch)
      createTagRelease()
      bumpVersion()
      close()
    }
  }

  private fun Grgit.createTagRelease() {
    tag.add { name = tagName }
    push { tags = true }
  }

  private fun Grgit.bumpVersion() {
    appVersionProvider.increasePatchVersion()
    add { patterns = mutableSetOf(".") }
    commit { message = commitMessage }
    push()
  }
}
