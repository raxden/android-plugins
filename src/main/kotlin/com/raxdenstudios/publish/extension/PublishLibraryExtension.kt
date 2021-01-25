package com.raxdenstudios.publish.extension

import com.raxdenstudios.publish.model.Repository
import com.raxdenstudios.publish.model.BintrayRepository

open class PublishLibraryExtension {
  var name = ""
  var description = ""
  var web = ""
  var username = ""
  var developerName: String = ""
  var developerEmail: String = ""
  var repository: Repository = BintrayRepository.default
}

