package com.raxdenstudios.versioning

import com.raxdenstudios.GradleTestKit
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.test.assertContains
import kotlin.test.assertFalse

class VersioningConfigurationCacheTest {

    @TempDir
    lateinit var testProjectDir: Path

    private val project by lazy { VersioningTestProject(testProjectDir) }

    private val printVersionCodeScript = """
        abstract class PrintVersionCodeTask : DefaultTask() {
            @get:Input abstract val code: Property<Int>
            @TaskAction fun print() = println("VERSION_CODE=" + code.get())
        }
        androidComponents {
            onVariants(selector().withName("debug")) { variant ->
                val output = variant.outputs.single()
                tasks.register<PrintVersionCodeTask>("printVersionCode") {
                    code.set(output.versionCode.map { it })
                }
            }
        }
    """.trimIndent()

    private fun run(vararg args: String) =
        GradleTestKit.runner(testProjectDir, "--configuration-cache", *args).build()

    @Test
    fun `Given the configuration cache When showVersionInfo runs twice Then the second run reuses the entry`() {
        project.settings("app")
        project.rootBuild(android = true)
        project.appModule("app", filePath = "../version.properties")
        project.versionFile("version.properties")

        val first = run(":app:showVersionInfo")
        val second = run(":app:showVersionInfo")

        assertContains(first.output, "Configuration cache entry stored")
        assertContains(second.output, "Reusing configuration cache")
    }

    @Test
    fun `Given a cached build When the file is increased Then the app versionCode picks up the new value`() {
        project.settings("app")
        project.rootBuild(android = true)
        project.appModule("app", filePath = "../version.properties", extraScript = printVersionCodeScript)
        project.versionFile("version.properties")

        val before = run("-q", ":app:printVersionCode")
        run(":app:increasePatchVersion")
        val after = run("-q", ":app:printVersionCode")

        assertContains(before.output, "VERSION_CODE=1030402")
        assertContains(after.output, "VERSION_CODE=1030500")
        assertFalse(after.output.contains("Reusing configuration cache"))
    }
}
