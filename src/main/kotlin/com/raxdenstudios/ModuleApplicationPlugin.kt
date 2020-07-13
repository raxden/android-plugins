package com.raxdenstudios

import org.gradle.api.Plugin
import org.gradle.api.Project

open class ModuleApplicationPlugin : Plugin<Project> {
  override fun apply(project: Project) {

    val module = Module.App
    project.configurePlugins(module)
    project.configureAndroid(module)
  }
}


