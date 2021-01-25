package com.raxdenstudios.publish.extension

import com.raxdenstudios.publish.model.Bintray

open class PublishMavenCentralExtension {
  var name = ""
  var description = ""
  var username = ""
  var website = ""
  var developerName = ""
  var email = ""
  var bintray: Bintray = Bintray()
}

