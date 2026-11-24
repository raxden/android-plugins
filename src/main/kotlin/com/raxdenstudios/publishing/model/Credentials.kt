package com.raxdenstudios.publishing.model

/**
 * Username and password of a remote Maven repository. Blank values fall back to the
 * `OSSRH_USERNAME` / `OSSRH_PASSWORD` Gradle properties or environment variables.
 */
data class Credentials(
    val username: String,
    val password: String
) {

    companion object {
        val default = Credentials(
            username = "",
            password = ""
        )
    }
}
