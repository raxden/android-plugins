package com.raxdenstudios.publish.model

data class Repository(
  val name: String,
  val url: String,
  val credentials: Credentials
) {

  companion object {
    val default = Repository(
      name = "",
      url = "",
      credentials = Credentials.default
    )
    val sonatype = Repository(
      name = "sonatype",
      url = "https://oss.sonatype.org/service/local/staging/deploy/maven2/",
      credentials = Credentials.default
    )
  }
}
