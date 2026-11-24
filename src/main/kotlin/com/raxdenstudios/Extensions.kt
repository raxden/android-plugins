package com.raxdenstudios

import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.provider.Provider
import org.gradle.api.publish.PublishingExtension
import org.gradle.plugins.signing.SigningExtension
import java.util.Properties

fun Properties.getPropertyOrDefault(key: String, default: String): String =
    getProperty(key) ?: default

fun Project.hasPlugin(plugin: String): Boolean =
    pluginManager.hasPlugin(plugin)

fun Project.withPlugin(plugin: String, block: () -> Unit) {
    pluginManager.withPlugin(plugin) { block() }
}

/**
 * Fails with a [GradleException] when [plugin] has not been applied to this project.
 *
 * Call it once the project is evaluated (e.g. inside `afterEvaluate`) so the order in which
 * plugins are applied does not matter.
 */
fun Project.checkPlugin(
    plugin: String,
    requiredBy: String,
    lazyMessage: () -> Any = {
        "Plugin '$requiredBy' requires plugin '$plugin' to be applied to project '$path'."
    },
) {
    if (!hasPlugin(plugin)) throw GradleException(lazyMessage().toString())
}

/**
 * Lazily resolves [key] from Gradle properties, falling back to the environment variable with
 * the same name.
 */
fun Project.gradlePropertyOrEnvironment(key: String): Provider<String> =
    providers.gradleProperty(key).orElse(providers.environmentVariable(key))

fun Project.withApplicationPlugin(block: () -> Unit) {
    withPlugin("com.android.application") { block() }
}

fun Project.withDynamicFeaturePlugin(block: () -> Unit) {
    withPlugin("com.android.dynamic-feature") { block() }
}

fun Project.withJavaPlatformPlugin(block: () -> Unit) {
    withPlugin("java-platform") { block() }
}

fun Project.withLibraryPlugin(block: () -> Unit) {
    withPlugin("com.android.library") { block() }
}

fun Project.withMavenPublishPlugin(block: () -> Unit) {
    withPlugin("maven-publish") { block() }
}

/**
 * Returns the extension [name] of this project, creating it with type [T] when it does not exist.
 * Fails with [IllegalStateException] when an extension with that name exists but is not a [T].
 */
inline fun <reified T : Any> Project.getOrCreateExtension(name: String): T =
    when (val existing = extensions.findByName(name)) {
        null -> extensions.create(name, T::class.java, this)
        is T -> existing
        else -> error(
            "Extension '$name' already exists with type ${existing::class.java.name}, " +
                    "but ${T::class.java.name} was requested."
        )
    }

/** Returns the extension [name] as [T], or `null` when it does not exist or has another type. */
inline fun <reified T : Any> Project.extensionByName(name: String): T? =
    extensions.findByName(name) as? T

fun Project.printInfo(message: String) {
    logger.info(message)
}

fun Task.printInfo(message: String) {
    logger.info(message)
}

fun Project.publishing(configure: PublishingExtension.() -> Unit): Unit =
    extensions.configure("publishing", configure)

fun Project.signing(configure: SigningExtension.() -> Unit): Unit =
    extensions.configure("signing", configure)
