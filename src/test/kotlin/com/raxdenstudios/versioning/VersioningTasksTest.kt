package com.raxdenstudios.versioning

import com.raxdenstudios.versioning.extension.VersioningExtension
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.test.assertEquals
import kotlin.test.assertSame

internal class VersioningTasksTest {

    @TempDir
    lateinit var tempDir: Path

    private val project by lazy {
        ProjectBuilder.builder()
            .withProjectDir(tempDir.toFile())
            .withGradleUserHomeDir(tempDir.resolve("gradle-home").toFile())
            .build()
    }

    private val tasks by lazy {
        val extension = project.extensions.create("versioning", VersioningExtension::class.java)
        VersioningTasks(project, extension, project.layout.projectDirectory.file(extension.filePath))
    }

    @Test
    fun `Given the tasks already registered for one kind When registering another kind Then the first registration stays`() {
        tasks.register(VersioningKind.APP)
        val first = project.tasks.names.toSet()
        val showVersionInfo = project.tasks.named("showVersionInfo").get()

        tasks.register(VersioningKind.LIBRARY)

        assertEquals(first, project.tasks.names.toSet())
        assertSame(showVersionInfo, project.tasks.named("showVersionInfo").get())
    }
}
