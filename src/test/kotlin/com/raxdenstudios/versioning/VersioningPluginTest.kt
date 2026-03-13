package com.raxdenstudios.versioning

import com.raxdenstudios.GradleTestKit
import com.raxdenstudios.TestProjectFixture
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.test.assertContains

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
}
