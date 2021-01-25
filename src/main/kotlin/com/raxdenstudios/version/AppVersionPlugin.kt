package com.raxdenstudios.version

import com.android.build.gradle.BaseExtension
import com.raxdenstudios.version.extension.AppVersionExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.getByType

open class AppVersionPlugin : VersionPlugin<AppVersionExtension>() {

  override fun Project.createExtension(): AppVersionExtension =
    extensions.create("versioning")

  override fun Project.configureProject() {
    pluginManager.withPlugin("com.android.application") { configure() }
    pluginManager.withPlugin("com.android.dynamic-feature") { configure() }
  }

  private fun Project.configure() {
    extensions.getByType<BaseExtension>().run {
      defaultConfig.versionName = fileVersionProvider.versionName
      defaultConfig.versionCode = fileVersionProvider.versionCode
    }
  }
}
