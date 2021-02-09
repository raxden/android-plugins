package com.raxdenstudios.publish.extension

import com.raxdenstudios.publish.model.Coordinates
import com.raxdenstudios.publish.model.Signing
import com.raxdenstudios.publish.model.Pom
import com.raxdenstudios.publish.model.Repository

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
