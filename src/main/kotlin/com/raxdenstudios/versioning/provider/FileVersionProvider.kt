package com.raxdenstudios.versioning.provider

import com.raxdenstudios.getPropertyOrDefault
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.Properties

class FileVersionProvider(
    private val versionFilePath: String
) {

    companion object {
        private const val MAJOR_DEFAULT_VALUE = 1000000
        private const val MINOR_DEFAULT_VALUE = 10000
        private const val PATCH_DEFAULT_VALUE = 100
        private const val MAJOR = "MAJOR"
        private const val MINOR = "MINOR"
        private const val PATCH = "PATCH"
        private const val DEV = "DEV"
        private const val DEFAULT = "0"
    }

    private val properties by lazy {
        Properties().apply {
            val fileInputStream = FileInputStream(versionFilePath)
            fileInputStream.use { input -> load(input) }
        }
    }
    val major: Int
        get() = properties.getPropertyOrDefault(MAJOR, DEFAULT).toInt()
    val minor: Int
        get() = properties.getPropertyOrDefault(MINOR, DEFAULT).toInt()
    val patch: Int
        get() = properties.getPropertyOrDefault(PATCH, DEFAULT).toInt()
    val dev: Int
        get() = properties.getPropertyOrDefault(DEV, DEFAULT).toInt()
    val versionCode: Int
        get() = major * MAJOR_DEFAULT_VALUE + minor * MINOR_DEFAULT_VALUE + patch * PATCH_DEFAULT_VALUE + dev
    val versionName: String
        get() = "${major}.${minor}.${patch}"

    override fun toString() = "$versionName($versionCode)"

    fun increaseDevVersion() {
        properties.setProperty(DEV, dev.inc().toString())
        properties.saveChanges()
    }

    fun increasePatchVersion() {
        properties.setProperty(PATCH, patch.inc().toString())
        properties.saveChanges()
    }

    fun increaseMinorVersion() {
        properties.setProperty(MINOR, minor.inc().toString())
        properties.saveChanges()
    }

    fun increaseMajorVersion() {
        properties.setProperty(MAJOR, major.inc().toString())
        properties.saveChanges()
    }

    fun resetDevVersion() {
        properties.setProperty(DEV, DEFAULT)
        properties.saveChanges()
    }

    fun resetPatchVersion() {
        properties.setProperty(PATCH, DEFAULT)
        properties.saveChanges()
    }

    fun resetMinorVersion() {
        properties.setProperty(MINOR, DEFAULT)
        properties.saveChanges()
    }

    fun resetMajorVersion() {
        properties.setProperty(MAJOR, DEFAULT)
        properties.saveChanges()
    }

    private fun Properties.saveChanges() {
        val fileOutputStream = FileOutputStream(versionFilePath)
        fileOutputStream.use { output -> store(output, null) }
    }
}
