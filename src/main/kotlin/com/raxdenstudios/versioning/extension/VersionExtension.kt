package com.raxdenstudios.versioning.extension

import com.raxdenstudios.versioning.model.Credentials

open class VersionExtension {
  var versionFilePath: String = "./version.properties"
  var credentials: Credentials = Credentials("", "")
}
