package com.raxdenstudios.version.extension

import com.raxdenstudios.version.model.Credentials

open class VersionExtension {
  var versionFilePath: String = "./version.properties"
  var credentials: Credentials = Credentials("", "")
}
