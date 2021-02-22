package com.raxdenstudios.versioning.provider

import com.raxdenstudios.getPropertyOrDefault
import com.raxdenstudios.versioning.extension.VersioningExtension
import org.gradle.api.Project
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.*

class FileVersionProvider(
  private val project: Project
) {

  private val extension: VersioningExtension by lazy {
    project.extensions.getByName("versioning") as VersioningExtension
  }
  private val properties by lazy {
    Properties().apply {
      val fileInputStream = FileInputStream(extension.versionFilePath)
      fileInputStream.use { input -> load(input) }
    }
  }
  val major: Int
    get() = properties.getPropertyOrDefault("MAJOR", "0").toInt()
  val minor: Int
    get() = properties.getPropertyOrDefault("MINOR", "0").toInt()
  val patch: Int
    get() = properties.getPropertyOrDefault("PATCH", "0").toInt()
  val dev: Int
    get() = properties.getPropertyOrDefault("DEV", "0").toInt()
  val versionCode: Int
    get() = major * 1000000 + minor * 10000 + patch * 100 + dev
  val versionName: String
    get() = "${major}.${minor}.${patch}"

  fun increaseMinorVersion() {
    properties.setProperty("MINOR", minor.inc().toString())
    properties.saveChanges()
  }

  fun resetPatchVersion() {
    properties.setProperty("PATCH", "0")
    properties.saveChanges()
  }

  fun increasePatchVersion() {
    properties.setProperty("PATCH", patch.inc().toString())
    properties.saveChanges()
  }

  private fun Properties.saveChanges() {
    val fileOutputStream = FileOutputStream(extension.versionFilePath)
    fileOutputStream.use { output ->
      store(output, null)
    }
  }
}
