package com.raxdenstudios.publish.model

data class Coordinates(
  val groupId: String,
  val artifactId: String,
  val version: String
) {

  companion object {
    val default = Coordinates(
      groupId = "",
      artifactId = "",
      version = ""
    )
  }
}
