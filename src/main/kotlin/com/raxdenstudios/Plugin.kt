package com.raxdenstudios

sealed class Plugin(
    val id: String
) {

    object MavenPublish : Plugin("org.gradle.maven-publish")
    object Signing : Plugin("org.gradle.signing")
}
