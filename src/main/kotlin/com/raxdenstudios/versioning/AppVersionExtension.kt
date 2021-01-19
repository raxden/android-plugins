package com.raxdenstudios.versioning

open class AppVersionExtension(
  var versionName: String = "",
  var versionCode: Int = 0,
  var versionFilePath: String = "./version.properties",
  var credentials: Credentials = Credentials("", "")
)

data class Credentials(
  var user: String = "",
  var password: String = ""
)
