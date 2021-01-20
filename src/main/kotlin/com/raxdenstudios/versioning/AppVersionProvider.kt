package com.raxdenstudios.versioning

import com.raxdenstudios.getPropertyOrDefault
import org.gradle.api.Project
import java.io.File
import java.io.FileInputStream
import java.util.*

class AppVersionProvider(
  private val project: Project
) {

  private val extension: AppVersionExtension by lazy {
    project.extensions.getByName("versioning") as AppVersionExtension
  }
  private val propertiesFile = File(extension.versionFilePath)
  private val properties by lazy { Properties().apply { load(FileInputStream(propertiesFile)) } }

  val major: Int
    get() = properties.getPropertyOrDefault("MAJOR", "0").toInt()
  val minor: Int
    get() = properties.getPropertyOrDefault("MINOR", "0").toInt()
  val patch: Int
    get() = properties.getPropertyOrDefault("PATCH", "0").toInt()
  val dev: Int
    get() = properties.getPropertyOrDefault("DEV", "0").toInt()
  val versionCode: Int
    get() {
      return if (extension.versionCode > 0) extension.versionCode
      else major * 1000000 + minor * 10000 + patch * 100 + dev
    }
  val versionName: String
    get() {
      return if (extension.versionName.isNotEmpty()) extension.versionName
      else "${major}.${minor}.${patch}"
    }

  fun increaseMinorVersion() {
    properties.setProperty("MINOR", minor.inc().toString())
    saveChanges()
  }

  fun resetPatchVersion() {
    properties.setProperty("PATCH", "0")
    saveChanges()
  }

  fun increasePatchVersion() {
    properties.setProperty("PATCH", patch.inc().toString())
    saveChanges()
  }

  private fun saveChanges() {
    val outputStreamWriter = propertiesFile.writer()
    properties.store(outputStreamWriter, null)
    outputStreamWriter.close()
  }
}
