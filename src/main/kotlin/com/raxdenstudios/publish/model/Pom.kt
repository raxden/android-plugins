package com.raxdenstudios.publish.model

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
