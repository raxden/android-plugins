package com.raxdenstudios.publishing.model

/**
 * Developer listed in the POM. Only [name] is required; a blank [id] or [email] is omitted.
 *
 * @property id Developer id, e.g. a GitHub login.
 * @property name Full name; developers with a blank name are ignored.
 * @property email Contact email.
 */
data class Developer(
    val id: String,
    val name: String,
    val email: String
) {

    companion object {
        val default = Developer(
            id = "",
            name = "",
            email = ""
        )
    }
}