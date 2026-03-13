package com.raxdenstudios.publishing.configurer

import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension

class RepositoryDataConfigurer(
    private val project: Project,
    extensionName: String,
) : PublishingConfigurer<PublishingExtension>(project, extensionName) {

    private val repository = extension.repository
    private val credentials = repository.credentials
    private val name: String = repository.name
    private val url: String = repository.url
    private val username: String
        get() {
            return when {
                credentials.username.isNotEmpty() -> credentials.username
                project.properties["OSSRH_USERNAME"] != null -> project.properties["OSSRH_USERNAME"] as String
                System.getenv("OSSRH_USERNAME") != null -> System.getenv("OSSRH_USERNAME")
                else -> ""
            }
        }
    private val password: String
        get() {
            return when {
                credentials.password.isNotEmpty() -> credentials.password
                project.properties["OSSRH_PASSWORD"] != null -> project.properties["OSSRH_PASSWORD"] as String
                System.getenv("OSSRH_PASSWORD") != null -> System.getenv("OSSRH_PASSWORD")
                else -> ""
            }
        }

    override fun configure(data: PublishingExtension) = data.run {
        repositories {
            maven {
                name = this@RepositoryDataConfigurer.name
                url = project.uri(this@RepositoryDataConfigurer.url)
                credentials {
                    username = this@RepositoryDataConfigurer.username
                    password = this@RepositoryDataConfigurer.password
                }
            }
        }
    }
}
