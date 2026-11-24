package com.raxdenstudios.publishing.configurer

import org.gradle.api.GradleException
import org.gradle.api.publish.maven.tasks.GenerateMavenPom
import org.gradle.api.tasks.TaskContainer
import org.gradle.kotlin.dsl.withType

/**
 * Fails the POM generation of [publicationName] (and therefore any publishing of it, local or
 * remote) with a clear message when [problems] is not empty. Configuring the project is never
 * blocked, so builds that do not publish are unaffected.
 *
 * [problems] is computed once the project is evaluated (see [PomDataConfigurer.problems]); the
 * task action only captures plain values, so it is configuration cache safe.
 */
class PomValidationConfigurer(
    private val projectPath: String,
    private val publicationName: String,
    private val problems: List<String>,
) {

    fun configure(tasks: TaskContainer) {
        if (problems.isEmpty()) return

        val taskName = "generatePomFileFor${publicationName.replaceFirstChar(Char::uppercase)}Publication"
        val message = "Cannot publish project '$projectPath':\n" +
            problems.joinToString(separator = "\n") { problem -> "  - $problem" }

        tasks.withType<GenerateMavenPom>()
            .matching { task -> task.name == taskName }
            .configureEach {
                doFirst { throw GradleException(message) }
            }
    }
}
