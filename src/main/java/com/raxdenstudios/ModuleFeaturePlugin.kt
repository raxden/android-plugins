package com.raxdenstudios

import org.gradle.api.Plugin
import org.gradle.api.Project

open class ModuleFeaturePlugin : Plugin<Project> {
  override fun apply(project: Project) {

    val module = Module.Feature
    project.configurePlugins(module)
    project.configureAndroid(module)
  }
}
