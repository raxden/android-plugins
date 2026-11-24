package com.raxdenstudios

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import java.nio.file.Path
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

object GradleTestKit {

    fun runner(
        projectDir: Path,
        vararg arguments: String
    ): GradleRunner = GradleRunner.create()
        .withProjectDir(projectDir.toFile())
        .withArguments(*arguments)
        .withPluginClasspath()
        .forwardOutput()

    fun assertTaskSucceeded(result: BuildResult, taskPath: String) {
        val task = result.task(taskPath)
        assertNotNull(task, "Task '$taskPath' was not found in the build result.")
        assertEquals(TaskOutcome.SUCCESS, task.outcome, "Task '$taskPath' did not succeed.")
    }
}