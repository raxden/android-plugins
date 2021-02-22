package com.raxdenstudios.reporting

import com.android.build.gradle.AppExtension
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.api.BaseVariant
import org.gradle.api.DomainObjectSet
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.gradle.testing.jacoco.plugins.JacocoPluginExtension
import org.gradle.testing.jacoco.plugins.JacocoTaskExtension
import org.gradle.testing.jacoco.tasks.JacocoReport

open class JacocoPlugin : Plugin<Project> {

  private val filters = JacocoFilters()

  override fun apply(project: Project) {

    project.configurePlugins()
    project.configureJacoco()
    project.configureProject()
  }

  private fun Project.configurePlugins() {
    plugins.apply("jacoco")
  }

  private fun Project.configureJacoco() {
    jacoco {
      toolVersion = "0.8.5"
    }

    tasks.withType(Test::class.java) {
      configure<JacocoTaskExtension> {
        // This needs to be set to true in case there are Roboelectric tests in the app.
        isIncludeNoLocationClasses = true
      }
    }
  }

  private fun Project.configureProject() {
    pluginManager.withPlugin("com.android.application") { configureApplication() }
    pluginManager.withPlugin("com.android.dynamic-feature") { configureApplication() }
    pluginManager.withPlugin("com.android.library") { configureLibrary() }
  }

  private fun Project.configureApplication() {
    afterEvaluate {
      val extension = extensions.getByType<AppExtension>()
      createJacocoTestReportForVariants(extension.applicationVariants)
    }
  }

  private fun Project.configureLibrary() {
    afterEvaluate {
      val extension = extensions.getByType<LibraryExtension>()
      createJacocoTestReportForVariants(extension.libraryVariants)
    }
  }

  private fun Project.createJacocoTestReportForVariants(variants: DomainObjectSet<out BaseVariant>) {
    variants.forEach { variant -> createJacocoTestReport(variant.name) }
  }

  private fun Project.createJacocoTestReport(variantName: String) {
    tasks.register("jacocoTestReport${variantName.capitalize()}", JacocoReport::class.java) {
      description = "Generate Jacoco coverage reports after running $variantName tests."
      group = "reporting"
      dependsOn("test${variantName.capitalize()}UnitTest")

      setSourceDirectories()
      setClassDirectories(variantName)
      setExecutionData(variantName)

      reports {
        xml.isEnabled = true
        html.isEnabled = true
      }

      doLast {
        println("View code coverage at: $buildDir/reports/jacoco/jacocoTestReport${variantName.capitalize()}/html/index.html")
      }
    }
  }

  private fun JacocoReport.setExecutionData(variantName: String) {
    executionData.setFrom(
      project.files("${project.buildDir}/jacoco/test${variantName.capitalize()}UnitTest.exec")
    )
  }

  private fun JacocoReport.setClassDirectories(variantName: String) {
    classDirectories.setFrom(
      project.files(
        project.fileTree("${project.buildDir}/intermediates/javac/$variantName") {
          exclude(filters.retrieve())
        },
        // Android Gradle Plugin 3.2.x support.
        project.fileTree("${project.buildDir}/tmp/kotlin-classes/${variantName}") {
          exclude(filters.retrieve())
        }
      )
    )
  }

  private fun JacocoReport.setSourceDirectories() {
    val androidExtension = project.extensions.findByName("android") as BaseExtension
    val paths = androidExtension.sourceSets.flatMap { sources -> sources.java.srcDirs }
    sourceDirectories.setFrom(project.files(paths))
  }

  private fun Project.jacoco(configure: JacocoPluginExtension.() -> Unit): Unit =
    (this as ExtensionAware).extensions.configure("jacoco", configure)
}

