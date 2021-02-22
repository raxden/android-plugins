package com.raxdenstudios.publishing.model

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
