package com.raxdenstudios.versioning

import java.io.File
import java.nio.file.Path

/**
 * Builds throwaway Gradle projects on disk for TestKit when the shared fixture does not fit.
 */
internal class VersioningTestProject(private val root: Path) {

    fun file(path: String): File = root.resolve(path).toFile()

    fun write(path: String, content: String): File =
        file(path).apply {
            parentFile.mkdirs()
            writeText(content)
        }

    fun readLines(path: String): List<String> = file(path).readLines()

    fun settings(vararg modules: String) {
        write(
            "settings.gradle.kts",
            """
            pluginManagement {
                repositories {
                    gradlePluginPortal()
                    google()
                    mavenCentral()
                }
            }
            dependencyResolutionManagement {
                repositories {
                    google()
                    mavenCentral()
                }
            }
            rootProject.name = "test-project"
            ${modules.joinToString("\n") { "include(\":$it\")" }}
            """.trimIndent()
        )
    }

    fun rootBuild(android: Boolean) {
        write(
            "build.gradle.kts",
            """
            plugins {
                ${if (android) "id(\"com.android.application\") apply false" else ""}
                id("com.raxdenstudios.android-versioning") apply false
            }
            """.trimIndent()
        )
    }

    fun versionFile(path: String, major: Int = 1, minor: Int = 3, patch: Int = 4, dev: Int = 2) {
        write(path, "MAJOR=$major\nMINOR=$minor\nPATCH=$patch\nDEV=$dev\n")
    }

    fun platformModule(name: String, filePath: String? = null) {
        write(
            "$name/build.gradle.kts",
            """
            plugins {
                `java-platform`
                id("com.raxdenstudios.android-versioning")
            }
            ${filePath?.let { "versioning { filePath = \"$it\" }" }.orEmpty()}
            """.trimIndent()
        )
    }

    fun appModule(name: String, filePath: String, extraScript: String = "") {
        write(
            "$name/build.gradle.kts",
            """
            plugins {
                id("com.android.application")
                id("com.raxdenstudios.android-versioning")
            }

            versioning { filePath = "$filePath" }

            android {
                namespace = "com.example.$name"
                compileSdk = 34
                defaultConfig {
                    applicationId = "com.example.$name"
                    minSdk = 24
                    targetSdk = 34
                }
            }

            $extraScript
            """.trimIndent()
        )
        write("$name/src/main/AndroidManifest.xml", "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<manifest />\n")
    }
}
