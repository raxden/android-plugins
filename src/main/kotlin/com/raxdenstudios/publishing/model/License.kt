package com.raxdenstudios.publishing.model

data class License(
  val name: String,
  val url: String
) {

  companion object {
    val default = License(
      name = "",
      url = ""
    )
    val apache2 = License(
      name = "The Apache License, Version 2.0",
      url = "http://www.apache.org/licenses/LICENSE-2.0.txt"
    )
  }
}
