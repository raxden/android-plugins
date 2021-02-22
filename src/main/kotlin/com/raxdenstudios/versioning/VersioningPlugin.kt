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

class VersioningPlugin: Plugin<Project> {

  private lateinit var versioningExtension: VersioningExtension
  private lateinit var fileVersionProvider: FileVersionProvider

  override fun apply(project: Project) {

    project.registerReleaseCandidateTask()
    project.registerReleaseCandidateTagTask()

    project.configureProject()
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

  private fun Project.configureProject() {
    pluginManager.withPlugin("com.android.application") {
      versioningExtension = extensions.create<AppVersioningExtension>("versioning")
      afterEvaluate { configure() }
    }
    pluginManager.withPlugin("com.android.library") {
      versioningExtension = extensions.create<LibraryVersioningExtension>("versioning")
      afterEvaluate { configure() }
    }
  }

  private fun Project.configure() {
    fileVersionProvider = FileVersionProvider(project)
    extensions.getByType<BaseExtension>().run {
      when (val extension = versioningExtension) {
        is AppVersioningExtension -> {
          defaultConfig.versionName = fileVersionProvider.versionName
          defaultConfig.versionCode = fileVersionProvider.versionCode
        }
        is LibraryVersioningExtension -> {
          version = fileVersionProvider.versionName
          group = extension.group
        }
      }
    }
  }
}
