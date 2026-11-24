package com.raxdenstudios.publishing.model

/**
 * Extra POM data. Its values take precedence over the `PublishExtension` properties: blank
 * [name], [description] and [url], empty [developers] and blank [scm] fields fall back to them.
 * `Pom.default` lists the Apache License 2.0 only.
 */
data class Pom(
    val name: String,
    val description: String,
    val url: String,
    val licenses: List<License>,
    val developers: List<Developer>,
    val scm: Scm
) {

    companion object {
        val default = Pom(
            name = "",
            description = "",
            url = "",
            licenses = listOf(License.apache2),
            developers = listOf(),
            scm = Scm.default
        )
    }
}
