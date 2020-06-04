package com.raxdenstudios

import Versions
import com.android.build.gradle.BaseExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType

sealed class Module {

  object App : Module()
  object Library : Module()
  object Feature : Module()
}

fun Project.configurePlugins(module: Module) {
  when (module) {
    is Module.App -> {
      plugins.apply("com.android.application")
      plugins.apply("kotlin-android")
      plugins.apply("kotlin-kapt")
      plugins.apply("kotlin-android-extensions")
    }
    is Module.Library, is Module.Feature -> {
      plugins.apply("com.android.library")
      plugins.apply("kotlin-android")
      plugins.apply("kotlin-kapt")
      plugins.apply("kotlin-android-extensions")
    }
  }
}

fun Project.configureAndroid(module: Module) {
  extensions.getByType<BaseExtension>().run {

    compileSdkVersion(Versions.compileSdk)

    compileOptions(module)
    defaultConfig(module)
    buildTypes(module)
    packagingOptions(module)
    buildFeatures(module)
  }
}

private fun BaseExtension.compileOptions(module: Module) {
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
  }
}

private fun BaseExtension.defaultConfig(module: Module) = when (module) {
  is Module.App ->
    defaultConfig {
      minSdkVersion(Versions.minSdk)
      targetSdkVersion(Versions.targetSdk)

      testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
  is Module.Library, is Module.Feature ->
    defaultConfig {
      minSdkVersion(Versions.minSdk)
      targetSdkVersion(Versions.targetSdk)

      testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
      consumerProguardFile("consumer-rules.pro")
    }
}

private fun BaseExtension.buildTypes(module: Module) = when (module) {
  is Module.App ->
    buildTypes {
      getByName("debug") {
        isMinifyEnabled = false
      }
      getByName("release") {
        isMinifyEnabled = true
        isShrinkResources = true
        proguardFiles(
          "proguard-android-optimize.txt",
          "proguard-rules.pro"
        )
      }
    }
  is Module.Library, is Module.Feature ->
    buildTypes {
      getByName("debug") {
        isMinifyEnabled = false
      }
    }
}

private fun BaseExtension.packagingOptions(module: Module) = when (module) {
  is Module.App ->
    packagingOptions {
      exclude("META-INF/*.kotlin_module")
    }
  is Module.Library, is Module.Feature ->
    packagingOptions {}
}

private fun BaseExtension.buildFeatures(module: Module) {
  buildFeatures.dataBinding = true
}