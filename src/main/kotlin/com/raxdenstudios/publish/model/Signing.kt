package com.raxdenstudios.publish.model

data class Signing(
  val keyId: String,
  val key: String,
  val password: String
) {

  companion object {
    val default = Signing(
      keyId = "",
      key = "",
      password = ""
    )
  }
}
