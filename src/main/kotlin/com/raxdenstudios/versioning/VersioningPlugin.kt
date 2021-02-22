package com.raxdenstudios.versioning

import com.android.build.gradle.BaseExtension
import com.raxdenstudios.versioning.extension.AppVersioningExtension
import com.raxdenstudios.versioning.extension.LibraryVersioningExtension
import com.raxdenstudios.versioning.extension.VersioningExtension
import com.raxdenstudios.versioning.provider.FileVersionProvider
import com.raxdenstudios.versioning.task.ReleaseCandidateBranchTask
import com.raxdenstudios.versioning.task.ReleaseCandidateTagTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register

class VersioningPlugin : Plugin<Project> {

  private lateinit var versioningExtension: VersioningExtension
  private lateinit var fileVersionProvider: FileVersionProvider

  override fun apply(project: Project) {
    project.initExtension()

    project.registerReleaseCandidateTask()
    project.registerReleaseCandidateTagTask()

    project.configure()
  }

  private fun Project.registerReleaseCandidateTagTask() {
    tasks.register<ReleaseCandidateTagTask>("releaseCandidateTag") {
      group = "versioning"
    }
  }

  private fun Project.registerReleaseCandidateTask() {
    tasks.register<ReleaseCandidateBranchTask>("releaseCandidate") {
      group = "versioning"
    }
  }

  private fun Project.initExtension() {
    pluginManager.withPlugin("com.android.application") { initAppVersioningExtension() }
    pluginManager.withPlugin("com.android.library") { initLibraryVersioningExtension() }
  }

  private fun Project.initLibraryVersioningExtension() {
    versioningExtension = extensions.create<LibraryVersioningExtension>("versioning")
  }

  private fun Project.initAppVersioningExtension() {
    versioningExtension = extensions.create<AppVersioningExtension>("versioning")
  }

  private fun Project.configure() {
    afterEvaluate {
      fileVersionProvider = FileVersionProvider(this)

      extensions.getByType<BaseExtension>().run {
        pluginManager.withPlugin("com.android.application") { configureApplication() }
        pluginManager.withPlugin("com.android.dynamic-feature") { configureApplication() }
        pluginManager.withPlugin("com.android.library") { configureLibrary() }
      }
    }
  }

  private fun Project.configureLibrary() {
    version = fileVersionProvider.versionName
    group = (versioningExtension as LibraryVersioningExtension).group
  }

  private fun BaseExtension.configureApplication() {
    defaultConfig.versionName = fileVersionProvider.versionName
    defaultConfig.versionCode = fileVersionProvider.versionCode
  }
}
