package com.raxdenstudios.publish.model

typealias BintrayRepository = Repository.Bintray

sealed class Repository {

  data class Bintray(
    val key: String,
    val name: String,
    val publish: Boolean = true,
    val override: Boolean = true
  ) : Repository() {

    companion object {
      val default = Bintray("", "")
    }
  }
}
