package com.raxdenstudios

import org.gradle.api.Project
import org.gradle.api.Task
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

fun Project.checkPlugin(
    plugin: String,
    lazyMessage: () -> Any = { "requires an `plugin` to be applied" }
) {
    check(hasPlugin(plugin), lazyMessage)
}

fun Project.checkPlatformPlugin() {
    checkPlugin("java-platform")
}

fun Project.checkLibraryPlugin() {
    checkPlugin("com.android.library")
}

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

inline fun <reified T : Any> Project.getOrCreateExtension(name: String): T =
    when (val existing = extensions.findByName(name)) {
        null -> extensions.create(name, T::class.java, this)
        is T -> existing
        else -> error(
            "Extension '$name' already exists with type ${existing::class.java.name}, " +
                    "but ${T::class.java.name} was requested."
        )
    }

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
