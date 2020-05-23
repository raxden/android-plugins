package com.rs.android.plugins

import com.rs.android.Module
import com.rs.android.configurePlugins
import com.rs.android.configureAndroid
import org.gradle.api.Plugin
import org.gradle.api.Project

open class ModuleFeaturePlugin : Plugin<Project> {
  override fun apply(project: Project) {

    val module = Module.Library
    project.configurePlugins(module)
    project.configureAndroid(module)
  }
}