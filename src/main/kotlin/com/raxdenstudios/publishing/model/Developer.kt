package com.raxdenstudios.publishing.model

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