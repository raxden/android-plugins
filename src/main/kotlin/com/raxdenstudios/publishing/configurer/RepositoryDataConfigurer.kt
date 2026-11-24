package com.raxdenstudios.publishing.configurer

import com.raxdenstudios.gradlePropertyOrEnvironment
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension

/**
 * Adds the remote Maven repository configured in the extension. Nothing is added when its url is
 * blank (the default): Maven Central Portal tooling such as NMCP consumes the publications
 * directly. Must run once the build script has been evaluated.
 *
 * Blank credentials fall back to the `OSSRH_USERNAME` / `OSSRH_PASSWORD` Gradle properties and then
 * to the environment variables with the same names. Credentials are set only when at least one of
 * them is not blank.
 */
class RepositoryDataConfigurer(
    private val project: Project,
    extensionName: String,
) : PublishingConfigurer<PublishingExtension>(project, extensionName) {

    companion object {
        private const val USERNAME_KEY = "OSSRH_USERNAME"
        private const val PASSWORD_KEY = "OSSRH_PASSWORD"
    }

    private val repository
        get() = extension.repository.get()

    private val username: String
        get() = repository.credentials.username
            .ifBlank { project.gradlePropertyOrEnvironment(USERNAME_KEY).orNull.orEmpty() }

    private val password: String
        get() = repository.credentials.password
            .ifBlank { project.gradlePropertyOrEnvironment(PASSWORD_KEY).orNull.orEmpty() }

    override fun configure(data: PublishingExtension) {
        val repository = repository
        if (repository.url.isBlank()) return

        val username = username
        val password = password
        data.repositories.maven {
            if (repository.name.isNotBlank()) name = repository.name
            url = project.uri(repository.url)
            if (username.isNotBlank() || password.isNotBlank()) {
                credentials {
                    this.username = username
                    this.password = password
                }
            }
        }
    }
}
