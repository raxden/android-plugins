package com.raxdenstudios

import org.gradle.api.Plugin
import org.gradle.api.Project

open class ModuleComponentPlugin : Plugin<Project> {
  override fun apply(project: Project) {

    val module = Module.Component
    project.configurePlugins(module)
    project.configureAndroid(module)
  }
}
