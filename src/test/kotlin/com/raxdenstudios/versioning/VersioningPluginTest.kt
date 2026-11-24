package com.raxdenstudios.versioning

import com.raxdenstudios.GradleTestKit
import com.raxdenstudios.TestProjectFixture
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.test.assertContains
import kotlin.test.assertEquals

class VersioningPluginTest {

    @TempDir
    lateinit var testProjectDir: Path

    @Test
    fun `given version properties when showVersionInfo runs then prints correct version from app`() {
        TestProjectFixture.copyFixture(
            fixturePath = "test-projects/versioning/generic",
            destinationDir = testProjectDir
        )

        val task = ":app:showVersionInfo"
        val result = GradleTestKit.runner(testProjectDir, task).build()

        GradleTestKit.assertTaskSucceeded(result, task)
        assertContains(result.output, "Versioning file: ../version.properties")
        assertContains(result.output, "Version name: 1.3.4")
        assertContains(result.output, "Version code: 1030402")
    }

    @Test
    fun `given version properties when showVersionInfo runs then prints correct version from library`() {
        TestProjectFixture.copyFixture(
            fixturePath = "test-projects/versioning/generic",
            destinationDir = testProjectDir
        )

        val task = ":lib:showVersionInfo"
        val result = GradleTestKit.runner(testProjectDir, task).build()

        GradleTestKit.assertTaskSucceeded(result, task)
        assertContains(result.output, "Versioning file: version.properties")
        assertContains(result.output, "Version: 1.3.4")
    }

    @Test
    fun `given version properties when showVersionInfo runs then prints correct version from platform`() {
        TestProjectFixture.copyFixture(
            fixturePath = "test-projects/versioning/generic",
            destinationDir = testProjectDir
        )

        val task = ":platform:showVersionInfo"
        val result = GradleTestKit.runner(testProjectDir, task).build()

        GradleTestKit.assertTaskSucceeded(result, task)
        assertContains(result.output, "Versioning file: version.properties")
        assertContains(result.output, "Version: 1.3.4")
    }

    @Test
    fun `given version properties when getVersion runs then prints correct version`() {
        TestProjectFixture.copyFixture(
            fixturePath = "test-projects/versioning/generic",
            destinationDir = testProjectDir
        )

        val task = ":app:getVersion"
        val result = GradleTestKit.runner(testProjectDir, task).build()

        GradleTestKit.assertTaskSucceeded(result, task)
        assertContains(result.output, "1.3.4")
    }

    @Test
    fun `given version properties when increaseDevVersion runs then prints correct version`() {
        TestProjectFixture.copyFixture(
            fixturePath = "test-projects/versioning/generic",
            destinationDir = testProjectDir
        )

        val task = ":app:increaseDevVersion"
        val result = GradleTestKit.runner(testProjectDir, task).build()

        GradleTestKit.assertTaskSucceeded(result, task)
    }

    @Test
    fun `given version properties when increaseMajorVersion runs then prints correct version`() {
        TestProjectFixture.copyFixture(
            fixturePath = "test-projects/versioning/generic",
            destinationDir = testProjectDir
        )

        val task = ":app:increaseMajorVersion"
        val result = GradleTestKit.runner(testProjectDir, task).build()

        GradleTestKit.assertTaskSucceeded(result, task)
    }

    @Test
    fun `given version properties when increaseMinorVersion runs then prints correct version`() {
        TestProjectFixture.copyFixture(
            fixturePath = "test-projects/versioning/generic",
            destinationDir = testProjectDir
        )

        val task = ":app:increaseMinorVersion"
        val result = GradleTestKit.runner(testProjectDir, task).build()

        GradleTestKit.assertTaskSucceeded(result, task)
    }

    @Test
    fun `given version properties when increasePatchVersion runs then prints correct version`() {
        TestProjectFixture.copyFixture(
            fixturePath = "test-projects/versioning/generic",
            destinationDir = testProjectDir
        )

        val task = ":app:increasePatchVersion"
        val result = GradleTestKit.runner(testProjectDir, task).build()

        GradleTestKit.assertTaskSucceeded(result, task)
    }

    @Test
    fun `Given the app fixture When getVersion runs quietly Then the last output line is exactly the version`() {
        TestProjectFixture.copyFixture(
            fixturePath = "test-projects/versioning/generic",
            destinationDir = testProjectDir
        )

        val result = GradleTestKit.runner(testProjectDir, "-q", ":app:getVersion").build()

        assertEquals("1.3.4", result.output.lines().last { it.isNotBlank() })
    }

    @Test
    fun `Given a java platform with a custom filePath and no default file When applied Then it does not crash`() {
        val project = VersioningTestProject(testProjectDir)
        project.settings("platform")
        project.rootBuild(android = false)
        project.platformModule("platform", filePath = "custom/v.properties")
        project.versionFile("platform/custom/v.properties")

        val result = GradleTestKit.runner(testProjectDir, "-q", ":platform:getVersion").build()

        assertEquals("1.3.4", result.output.lines().last { it.isNotBlank() })
    }

    @Test
    fun `Given a java platform When showVersionInfo runs Then the version is exposed as project version`() {
        val project = VersioningTestProject(testProjectDir)
        project.settings("platform")
        project.rootBuild(android = false)
        project.platformModule("platform")
        project.versionFile("platform/version.properties")
        project.write(
            "platform/build.gradle.kts",
            project.file("platform/build.gradle.kts").readText() +
                "\ntasks.register(\"printProjectVersion\") { doLast { println(\"PROJECT_VERSION=\" + project.version) } }\n"
        )

        val result = GradleTestKit.runner(testProjectDir, "-q", ":platform:printProjectVersion").build()

        assertContains(result.output, "PROJECT_VERSION=1.3.4")
    }

    @Test
    fun `Given a missing version file When getVersion runs Then it fails with a clear message`() {
        val project = VersioningTestProject(testProjectDir)
        project.settings("platform")
        project.rootBuild(android = false)
        project.platformModule("platform", filePath = "nope.properties")

        val result = GradleTestKit.runner(testProjectDir, ":platform:getVersion").buildAndFail()

        assertContains(result.output, "nope.properties")
        assertContains(result.output, "does not exist")
    }

    @Test
    fun `Given a missing version file When showVersionInfo runs Then it fails with the plugin message and the path`() {
        val project = VersioningTestProject(testProjectDir)
        project.settings("platform")
        project.rootBuild(android = false)
        project.platformModule("platform", filePath = "nope.properties")

        val result = GradleTestKit.runner(testProjectDir, ":platform:showVersionInfo").buildAndFail()

        assertContains(result.output, "nope.properties")
        assertContains(result.output, "does not exist")
    }

    @Test
    fun `Given an invalid version file When getVersion runs Then it fails naming the key and the file`() {
        val project = VersioningTestProject(testProjectDir)
        project.settings("platform")
        project.rootBuild(android = false)
        project.platformModule("platform")
        project.write("platform/version.properties", "MAJOR=1\nMINOR=abc\n")

        val result = GradleTestKit.runner(testProjectDir, ":platform:getVersion").buildAndFail()

        assertContains(result.output, "MINOR must be an integer")
        assertContains(result.output, "version.properties")
    }

    @Test
    fun `Given a project without a supported plugin When versioning is applied Then it warns and does not crash`() {
        val project = VersioningTestProject(testProjectDir)
        project.settings("empty")
        project.rootBuild(android = false)
        project.write("empty/build.gradle.kts", "plugins { id(\"com.raxdenstudios.android-versioning\") }\n")

        val result = GradleTestKit.runner(testProjectDir, ":empty:tasks").build()

        assertContains(result.output, "none of")
        assertContains(result.output, "versioning tasks are not registered")
    }
}
