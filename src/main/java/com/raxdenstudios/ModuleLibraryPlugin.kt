package com.raxdenstudios

import org.gradle.api.Plugin
import org.gradle.api.Project

open class ModuleLibraryPlugin : Plugin<Project> {
  override fun apply(project: Project) {

    val module = Module.Library
    project.configurePlugins(module)
    project.configureAndroid(module)
  }
}
