package com.raxdenstudios.publish

open class PublishMavenCentralExtension {
  var name = ""
  var description = ""
  var username = ""
  var website = ""
  var developerName = ""
  var email = ""
  var bintray: Bintray = Bintray()
}

class Bintray(
  var key: String = "",
  var repository: String = ""
)
