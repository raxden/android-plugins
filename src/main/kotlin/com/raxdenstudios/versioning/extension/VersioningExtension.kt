package com.raxdenstudios.versioning.extension

import com.raxdenstudios.versioning.model.GitCredentials

open class VersioningExtension {
  var versionFilePath: String = "./version.properties"
  var credentials: GitCredentials = GitCredentials("", "")
}
