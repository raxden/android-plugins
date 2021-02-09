package com.raxdenstudios.publish.provider

import com.android.build.gradle.LibraryExtension
import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.register

class ArtifactsProvider(
  private val project: Project
): DataProvider(project) {

  fun configure(publication: MavenPublication) {
    publication.run {
      artifact(project.getAndroidSourcesJar())
    }
  }

  private fun Project.getAndroidSourcesJar(): Jar {
    return tasks.register<Jar>("androidSourcesJar") {
      archiveClassifier.set("sources")
      from((project.extensions.findByName("android") as LibraryExtension).sourceSets["main"].java.srcDirs)
    }.get()
  }
}
