package com.raxdenstudios.publishing.provider

import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.publish.PublishingExtension
import org.gradle.plugins.signing.SigningExtension

class SigningDataProvider(
  private val project: Project
): DataProvider(project) {

  private val Project.publishing: PublishingExtension
    get() = (this as ExtensionAware).extensions.getByName("publishing") as PublishingExtension
  private val signing = extension.signing
  private val keyId: String
    get() {
      return when {
        signing.keyId.isNotEmpty() -> signing.keyId
        project.properties["SIGNING_KEY_ID"] != null -> project.properties["SIGNING_KEY_ID"] as String
        System.getenv("SIGNING_KEY_ID") != null -> System.getenv("SIGNING_KEY_ID")
        else -> ""
      }
    }
  private val key: String
    get() {
      return when {
        signing.key.isNotEmpty() -> signing.key
        project.properties["SIGNING_KEY"] != null -> project.properties["SIGNING_KEY"] as String
        System.getenv("SIGNING_KEY") != null -> System.getenv("SIGNING_KEY")
        else -> ""
      }
    }
  private val password: String
    get() {
      return when {
        signing.password.isNotEmpty() -> signing.password
        project.properties["SIGNING_PASSWORD"] != null -> project.properties["SIGNING_PASSWORD"] as String
        System.getenv("SIGNING_PASSWORD") != null -> System.getenv("SIGNING_PASSWORD")
        else -> ""
      }
    }

  @Suppress("UnstableApiUsage")
  fun configure(extension: SigningExtension) {
    extension.run {
      useInMemoryPgpKeys(keyId, key, password)
      sign(project.publishing.publications)
    }
  }
}
