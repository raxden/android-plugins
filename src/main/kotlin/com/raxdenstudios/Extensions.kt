package com.raxdenstudios

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.ExtensionAware
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

fun Project.checkLibraryPlugin() {
    checkPlugin("com.android.library")
}

fun Project.withApplicationPlugin(block: () -> Unit) {
    withPlugin("com.android.application") { block() }
}

fun Project.withDynamicFeaturePlugin(block: () -> Unit) {
    withPlugin("com.android.dynamic-feature") { block() }
}

fun Project.withLibraryPlugin(block: () -> Unit) {
    withPlugin("com.android.library") { block() }
}

fun Project.extensionByName(name: String): Any? =
    extensions.findByName(name) ?: rootProject.extensions.findByName(name)

fun Project.printInfo(message: String) {
    logger.info(message)
}

fun Task.printInfo(message: String) {
    logger.info(message)
}

fun Project.publishing(configure: PublishingExtension.() -> Unit): Unit =
    (this as ExtensionAware).extensions.configure("publishing", configure)

fun Project.signing(configure: SigningExtension.() -> Unit): Unit =
    (this as ExtensionAware).extensions.configure("signing", configure)
