package com.raxdenstudios.versioning

import com.raxdenstudios.GradleTestKit
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.test.assertContains
import kotlin.test.assertEquals

class VersioningIncreaseTasksTest {

    @TempDir
    lateinit var testProjectDir: Path

    private val project by lazy { VersioningTestProject(testProjectDir) }

    private fun platformProject(major: Int = 1, minor: Int = 3, patch: Int = 4, dev: Int = 2) {
        project.settings("platform")
        project.rootBuild(android = false)
        project.platformModule("platform")
        project.versionFile("platform/version.properties", major, minor, patch, dev)
    }

    private fun assertIncrease(task: String, message: String, expected: List<String>) {
        platformProject()

        val result = GradleTestKit.runner(testProjectDir, ":platform:$task").build()

        GradleTestKit.assertTaskSucceeded(result, ":platform:$task")
        assertContains(result.output, message)
        assertEquals(expected, project.readLines("platform/version.properties"))
    }

    @Test
    fun `Given a version file When increaseMajorVersion runs Then major is bumped and the rest reset`() =
        assertIncrease(
            "increaseMajorVersion",
            "Increasing major version",
            listOf("MAJOR=2", "MINOR=0", "PATCH=0", "DEV=0"),
        )

    @Test
    fun `Given a version file When increaseMinorVersion runs Then minor is bumped and patch and dev reset`() =
        assertIncrease(
            "increaseMinorVersion",
            "Increasing minor version",
            listOf("MAJOR=1", "MINOR=4", "PATCH=0", "DEV=0"),
        )

    @Test
    fun `Given a version file When increasePatchVersion runs Then patch is bumped and dev reset`() =
        assertIncrease(
            "increasePatchVersion",
            "Increasing patch version",
            listOf("MAJOR=1", "MINOR=3", "PATCH=5", "DEV=0"),
        )

    @Test
    fun `Given a version file When increaseDevVersion runs Then only dev is bumped`() =
        assertIncrease(
            "increaseDevVersion",
            "Increasing dev version",
            listOf("MAJOR=1", "MINOR=3", "PATCH=4", "DEV=3"),
        )

    @Test
    fun `Given DEV 99 When increaseDevVersion runs Then it fails clearly and leaves the file unchanged`() {
        platformProject(dev = 99)
        val before = project.file("platform/version.properties").readText()

        val result = GradleTestKit.runner(testProjectDir, ":platform:increaseDevVersion").buildAndFail()

        assertContains(result.output, "Cannot increase dev version")
        assertContains(result.output, "DEV must be between 0 and 99 (was 100)")
        assertEquals(before, project.file("platform/version.properties").readText())
    }

    @Test
    fun `Given a missing version file When increasePatchVersion runs Then it fails with a clear message`() {
        project.settings("platform")
        project.rootBuild(android = false)
        project.platformModule("platform")

        val result = GradleTestKit.runner(testProjectDir, ":platform:increasePatchVersion").buildAndFail()

        assertContains(result.output, "does not exist")
    }

    @Test
    fun `Given an invalid version file When increasePatchVersion runs Then it fails naming the key`() {
        project.settings("platform")
        project.rootBuild(android = false)
        project.platformModule("platform")
        project.write("platform/version.properties", "PATCH=x\n")

        val result = GradleTestKit.runner(testProjectDir, ":platform:increasePatchVersion").buildAndFail()

        assertContains(result.output, "PATCH must be an integer")
    }

    @Test
    fun `Given two app modules sharing one file When increasePatchVersion runs Then the file is bumped once`() {
        project.settings("app1", "app2")
        project.rootBuild(android = true)
        project.appModule("app1", filePath = "../version.properties")
        project.appModule("app2", filePath = "../version.properties")
        project.versionFile("version.properties")

        val result = GradleTestKit.runner(testProjectDir, "increasePatchVersion").build()

        GradleTestKit.assertTaskSucceeded(result, ":app1:increasePatchVersion")
        GradleTestKit.assertTaskSucceeded(result, ":app2:increasePatchVersion")
        assertEquals(listOf("MAJOR=1", "MINOR=3", "PATCH=5", "DEV=0"), project.readLines("version.properties"))
        assertContains(result.output, "was already increased in this build")
    }

    @Test
    fun `Given two modules sharing one file When increasePatchVersion runs in parallel Then the file is bumped once`() {
        project.settings("platform1", "platform2")
        project.rootBuild(android = false)
        project.platformModule("platform1", filePath = "../version.properties")
        project.platformModule("platform2", filePath = "../version.properties")
        project.versionFile("version.properties")

        val result = GradleTestKit.runner(testProjectDir, "increasePatchVersion", "--parallel").build()

        GradleTestKit.assertTaskSucceeded(result, ":platform1:increasePatchVersion")
        GradleTestKit.assertTaskSucceeded(result, ":platform2:increasePatchVersion")
        assertEquals(listOf("MAJOR=1", "MINOR=3", "PATCH=5", "DEV=0"), project.readLines("version.properties"))
        assertEquals(1, Regex("was already increased in this build").findAll(result.output).count())
    }

    private val minorThenPatch = listOf("MAJOR=1", "MINOR=4", "PATCH=1", "DEV=0")

    @Test
    fun `Given one module When minor and patch are requested in natural order Then the file ends at 1_4_1`() {
        platformProject()

        GradleTestKit.runner(testProjectDir, "increaseMinorVersion", "increasePatchVersion").build()

        assertEquals(minorThenPatch, project.readLines("platform/version.properties"))
    }

    @Test
    fun `Given one module When minor and patch are requested in reverse order Then the file ends at 1_4_1`() {
        platformProject()

        GradleTestKit.runner(testProjectDir, "increasePatchVersion", "increaseMinorVersion").build()

        assertEquals(minorThenPatch, project.readLines("platform/version.properties"))
    }

    @Test
    fun `Given the configuration cache When minor and patch are requested Then the file ends at 1_4_1`() {
        platformProject()

        GradleTestKit.runner(
            testProjectDir, "--configuration-cache", "increasePatchVersion", "increaseMinorVersion"
        ).build()

        assertEquals(minorThenPatch, project.readLines("platform/version.properties"))
    }

    @Test
    fun `Given two modules sharing one file When minor and patch are requested Then each part is applied once`() {
        project.settings("platform1", "platform2")
        project.rootBuild(android = false)
        project.platformModule("platform1", filePath = "../version.properties")
        project.platformModule("platform2", filePath = "../version.properties")
        project.versionFile("version.properties")

        val result = GradleTestKit.runner(testProjectDir, "increaseMinorVersion", "increasePatchVersion").build()

        assertEquals(minorThenPatch, project.readLines("version.properties"))
        assertContains(result.output, "was already increased in this build")
    }

    @Test
    fun `Given a failing increase under continue When another increase follows Then it still runs and the file is intact`() {
        platformProject(patch = 99)

        val result = GradleTestKit.runner(
            testProjectDir, "--continue", ":platform:increasePatchVersion", ":platform:increaseDevVersion"
        ).buildAndFail()

        assertContains(result.output, "Cannot increase patch version")
        assertEquals(TaskOutcome.FAILED, result.task(":platform:increasePatchVersion")?.outcome)
        assertEquals(TaskOutcome.SUCCESS, result.task(":platform:increaseDevVersion")?.outcome)
        assertEquals(
            listOf("MAJOR=1", "MINOR=3", "PATCH=99", "DEV=3"),
            project.readLines("platform/version.properties"),
        )
    }
}
