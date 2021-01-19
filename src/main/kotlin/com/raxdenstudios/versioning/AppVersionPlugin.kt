package com.raxdenstudios.versioning

import com.android.build.gradle.BaseExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register

open class AppVersionPlugin : Plugin<Project> {

  private lateinit var extension: AppVersionExtension
  private lateinit var appVersionProvider: AppVersionProvider
  private lateinit var appVersionCredentialsProvider: AppVersionCredentialsProvider

  override fun apply(project: Project) {
    extension = project.extensions.create("versioning")

    project.registerPrintVersionsTask()
    project.registerReleaseCandidateBranchTask()
    project.registerReleaseCandidateTagTask()

    project.afterEvaluate {
      appVersionProvider = AppVersionProvider(this)
      appVersionCredentialsProvider = AppVersionCredentialsProvider(this)

      pluginManager.withPlugin("com.android.application") { configure() }
      pluginManager.withPlugin("com.android.dynamic-feature") { configure() }
      pluginManager.withPlugin("com.android.library") { configureLibrary() }
    }
  }

  private fun Project.registerReleaseCandidateTagTask() {
    tasks.register<ReleaseCandidateTagTask>("releaseCandidateTag")
  }

  private fun Project.registerReleaseCandidateBranchTask() {
    tasks.register<CreateReleaseCandidateBranchTask>("releaseCandidate")
  }

  private fun Project.registerPrintVersionsTask() {
    tasks.register("printVersions") {
      doLast {
        println("versionCode: ${appVersionProvider.versionCode}")
        println("versionName: ${appVersionProvider.versionName}")
      }
    }
  }

  private fun Project.configure() {
    extensions.getByType<BaseExtension>().run {
      defaultConfig.versionName = appVersionProvider.versionName
      defaultConfig.versionCode = appVersionProvider.versionCode
    }
  }

  private fun Project.configureLibrary() {
    extensions.getByType<BaseExtension>().run {
      version = appVersionProvider.versionName
    }
  }
}
