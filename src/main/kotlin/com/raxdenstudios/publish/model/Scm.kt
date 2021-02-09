package com.raxdenstudios.publish.model

data class Scm(
  val connection: String,
  val developerConnection: String,
  val url: String
) {

  companion object {
    val default = Scm(
      connection = "",
      developerConnection = "",
      url = ""
    )
    fun withUrl(url: String) = Scm(
      connection = "scm:git:${url.removePrefix("https://")}.git",
      developerConnection = "scm:git:ssh://${url.removePrefix("https://")}.git",
      url = "$url/tree/master"
    )
  }
}
