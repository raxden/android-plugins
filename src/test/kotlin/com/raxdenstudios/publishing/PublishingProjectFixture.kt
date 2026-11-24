package com.raxdenstudios.publishing

import com.raxdenstudios.GradleTestKit
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Assumptions.assumeTrue
import java.io.File
import java.nio.file.Path

/** Builds throwaway multi-module projects (Android library + java-platform) for TestKit tests. */
internal class PublishingProjectFixture(private val root: Path) {

    val libDir: File get() = root.resolve("lib").toFile()
    val bomDir: File get() = root.resolve("bom").toFile()
    val localMavenDir: File get() = root.resolve("local-m2").toFile()
    val customRepoDir: File get() = root.resolve("custom-repo").toFile()

    /**
     * Writes settings and root files. With [includeLibrary] `false` only `:bom` is included and no
     * Android SDK is needed, which keeps the build fast.
     */
    fun writeSettings(includeLibrary: Boolean = true) {
        write(
            "settings.gradle.kts",
            """
            rootProject.name = "sample"
            dependencyResolutionManagement {
                repositories {
                    google()
                    mavenCentral()
                }
            }
            include(${if (includeLibrary) "\":lib\", " else ""}":bom")
            """.trimIndent()
        )
        write("build.gradle.kts", "")
        if (includeLibrary) {
            val sdkDir = androidSdkDir()
            assumeTrue(sdkDir != null, "Android SDK not found (set ANDROID_HOME or ANDROID_SDK_ROOT): skipping.")
            write("local.properties", "sdk.dir=$sdkDir")
            write("gradle.properties", "android.useAndroidX=true\n")
        }
    }

    /** Writes [content] to [relativePath] below the project root. */
    fun writeFile(relativePath: String, content: String): File {
        write(relativePath, content)
        return root.resolve(relativePath).toFile()
    }

    fun writeLibrary(
        pluginsBlock: String = LIBRARY_PLUGINS,
        publishBlock: String = DEFAULT_LIBRARY_PUBLISH,
        projectConfig: String = "",
    ) {
        write(
            "lib/build.gradle.kts",
            """
            import com.raxdenstudios.publishing.model.*
            import com.raxdenstudios.publishing.model.Credentials
            import com.raxdenstudios.publishing.model.Repository

            plugins {
            $pluginsBlock
            }

            $projectConfig

            android {
                namespace = "com.example.lib"
                compileSdk = 34
                defaultConfig { minSdk = 24 }
            }

            $publishBlock
            """.trimIndent()
        )
        write("lib/src/main/AndroidManifest.xml", "<manifest />")
    }

    fun writePlatform(
        publishBlock: String = DEFAULT_PLATFORM_PUBLISH,
        projectConfig: String = "",
        extraPlugins: String = "",
    ) {
        write(
            "bom/build.gradle.kts",
            """
            import com.raxdenstudios.publishing.model.*
            import com.raxdenstudios.publishing.model.Credentials
            import com.raxdenstudios.publishing.model.Repository

            plugins {
                id("java-platform")
                id("com.raxdenstudios.android-publish-platform")
                $extraPlugins
            }

            $projectConfig

            $publishBlock
            """.trimIndent()
        )
    }

    fun runner(vararg arguments: String): GradleRunner = runner(configurationCache = false, *arguments)

    fun runnerWithConfigurationCache(vararg arguments: String): GradleRunner =
        runner(configurationCache = true, *arguments)

    private fun runner(configurationCache: Boolean, vararg arguments: String): GradleRunner {
        val environment = System.getenv().filterKeys { key -> key !in SECRET_KEYS }
        // The user's gradle.properties may define real keys: command line values win. A key the
        // test passes itself is not blanked, since an empty -P value would still count as present.
        val blanking = SECRET_KEYS
            .filter { key -> arguments.none { argument -> argument.startsWith("-P$key=") } }
            .map { key -> "-P$key=" }
        return GradleTestKit.runner(
            root,
            *arguments,
            if (configurationCache) "--configuration-cache" else "--no-configuration-cache",
            "--stacktrace",
            *blanking.toTypedArray(),
        ).withEnvironment(environment)
    }

    private fun write(relativePath: String, content: String) {
        val file = root.resolve(relativePath).toFile()
        file.parentFile.mkdirs()
        file.writeText(content)
    }

    private fun androidSdkDir(): String? = listOfNotNull(
        System.getenv("ANDROID_HOME"),
        System.getenv("ANDROID_SDK_ROOT"),
        System.getProperty("user.home") + "/Library/Android/sdk",
        System.getProperty("user.home") + "/Android/Sdk",
    ).firstOrNull { path -> File(path).isDirectory }

    companion object {
        private val SECRET_KEYS =
            listOf("SIGNING_KEY", "SIGNING_KEY_ID", "SIGNING_PASSWORD", "OSSRH_USERNAME", "OSSRH_PASSWORD")

        const val LIBRARY_PLUGINS = """
    id("com.android.library")
    id("com.raxdenstudios.android-publish-library")
"""

        val DEFAULT_LIBRARY_PUBLISH = """
            group = "com.example"
            version = "1.2.3"

            publishLibrary {
                name = "Android"
                description = "Android Core"
                url = "https://github.com/raxden/android-commons"
                developer = Developer(id = "raxden", name = "Dev", email = "dev@example.com")
                coordinates = Coordinates.default.copy(artifactId = "commons-core")
            }
        """.trimIndent()

        val DEFAULT_PLATFORM_PUBLISH = """
            group = "com.example"
            version = "1.2.3"

            publishPlatform {
                name = "BOM"
                description = "Commons BOM"
                url = "https://github.com/raxden/android-commons"
                developer = Developer(id = "raxden", name = "Dev", email = "dev@example.com")
                coordinates = Coordinates.default.copy(artifactId = "commons-bom")
            }
        """.trimIndent()
    }
}
