package com.raxdenstudios.publishing.configurer

import com.raxdenstudios.gradlePropertyOrEnvironment
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.execution.TaskExecutionGraph
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.tasks.PublishToMavenRepository
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType
import org.gradle.plugins.signing.Sign
import org.gradle.plugins.signing.SigningExtension
import java.util.concurrent.Callable

/**
 * Signs every publication of the project. Must run once the build script has been evaluated.
 *
 * Keys come from the extension, falling back to the `SIGNING_KEY_ID`, `SIGNING_KEY` and
 * `SIGNING_PASSWORD` Gradle properties or environment variables. When no key is available
 * nothing is signed, which is fine for `publishToMavenLocal`; signing is required (and fails
 * fast) only when a task publishing to a Maven repository is going to run.
 */
class SigningDataConfigurer(
    private val project: Project,
    extensionName: String,
) : PublishingConfigurer<SigningExtension>(project, extensionName) {

    companion object {
        private const val KEY_ID_KEY = "SIGNING_KEY_ID"
        private const val KEY_KEY = "SIGNING_KEY"
        private const val PASSWORD_KEY = "SIGNING_PASSWORD"
    }

    private val signing
        get() = extension.signing.get()

    private val keyId: String
        get() = signing.keyId.ifBlank { project.gradlePropertyOrEnvironment(KEY_ID_KEY).orNull.orEmpty() }

    private val key: String
        get() = signing.key.ifBlank { project.gradlePropertyOrEnvironment(KEY_KEY).orNull.orEmpty() }

    private val password: String
        get() = signing.password.ifBlank { project.gradlePropertyOrEnvironment(PASSWORD_KEY).orNull.orEmpty() }

    override fun configure(data: SigningExtension): Unit = data.run {
        val key = key
        if (key.isNotBlank()) {
            val keyId = keyId
            if (keyId.isBlank()) useInMemoryPgpKeys(key, password) else useInMemoryPgpKeys(keyId, key, password)
        }

        // Configuration cache safe: Gradle resolves `required` when storing the entry, after the
        // task graph is calculated, and an entry is only reused for the same requested tasks.
        val taskGraph = project.gradle.taskGraph
        setRequired(Callable { taskGraph.publishesToMavenRepository() })
        sign(project.extensions.getByType<PublishingExtension>().publications)

        failFastWithoutSignatory()
    }

    /**
     * Whether the task graph contains a task that publishes to a Maven repository. Answers `true`
     * while the graph is not ready yet, so signing stays required until the tasks are known.
     */
    private fun TaskExecutionGraph.publishesToMavenRepository(): Boolean =
        runCatching { allTasks.any { task -> task is PublishToMavenRepository } }
            // The graph is not ready yet: be strict until it is.
            .getOrDefault(true)

    private fun failFastWithoutSignatory() {
        val projectPath = project.path
        project.tasks.withType<Sign>().configureEach {
            doFirst {
                if ((this as Sign).signatory == null) {
                    throw GradleException(
                        "Signing is required to publish project '$projectPath' to a remote repository, " +
                            "but no signing key is configured. Set the $KEY_KEY and $PASSWORD_KEY " +
                            "(and optionally $KEY_ID_KEY) Gradle properties or environment variables."
                    )
                }
            }
        }
    }
}
