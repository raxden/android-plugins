package com.raxdenstudios.publishing.extension

import com.raxdenstudios.publishing.model.Coordinates
import com.raxdenstudios.publishing.model.Signing
import com.raxdenstudios.publishing.model.Pom
import com.raxdenstudios.publishing.model.Repository

open class PublishLibraryExtension {
  var name = ""
  var description = ""
  var url = ""
  var developerId = ""
  var developerName: String = ""
  var developerEmail: String = ""
  var coordinates: Coordinates = Coordinates.default
  var pom: Pom = Pom.default
  var repository: Repository = Repository.sonatype
  var signing: Signing = Signing.default
}
