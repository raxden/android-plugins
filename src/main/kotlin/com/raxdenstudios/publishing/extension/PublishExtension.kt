package com.raxdenstudios.publishing.extension

import com.raxdenstudios.publishing.model.Coordinates
import com.raxdenstudios.publishing.model.Developer
import com.raxdenstudios.publishing.model.Pom
import com.raxdenstudios.publishing.model.Repository
import com.raxdenstudios.publishing.model.Signing
import org.gradle.api.Project
import javax.inject.Inject

open class PublishExtension @Inject constructor(
    val project: Project
) {

    var name = ""
    var description = ""
    var url = ""
    var developer: Developer = Developer.default
    var coordinates: Coordinates = Coordinates.default
    var pom: Pom = Pom.default
    var repository: Repository = Repository.sonatype
    var signing: Signing = Signing.default
}
