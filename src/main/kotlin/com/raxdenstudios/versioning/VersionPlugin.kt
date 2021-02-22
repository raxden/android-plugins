package com.raxdenstudios.versioning

import com.raxdenstudios.versioning.extension.VersionExtension
import com.raxdenstudios.versioning.provider.FileVersionProvider
import com.raxdenstudios.versioning.task.ReleaseCandidateBranchTask
import com.raxdenstudios.versioning.task.ReleaseCandidateTagTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.register

abstract class VersionPlugin<Extension : VersionExtension> : Plugin<Project> {

  protected lateinit var extension: Extension
  protected lateinit var fileVersionProvider: FileVersionProvider

  override fun apply(project: Project) {
    extension = project.createExtension()

//    project.tasks.register<FileVersionProviderTask>("fileVersionProvider") {
//      group = "versioning"
//    }
    project.tasks.register<ReleaseCandidateBranchTask>("releaseCandidate") {
      group = "versioning"
    }
    project.tasks.register<ReleaseCandidateTagTask>("releaseCandidateTag") {
      group = "versioning"
    }

    project.afterEvaluate {
      fileVersionProvider = FileVersionProvider(project)

      configureProject()
    }
  }

  protected abstract fun Project.createExtension(): Extension

  protected abstract fun Project.configureProject()
}
