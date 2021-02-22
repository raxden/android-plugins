package com.raxdenstudios.reporting

class JacocoFilters {

  private val genericExcludes = listOf(
    "**/*_*.class"
  )

  private val androidGeneratedClasses = listOf(
    "**/R.class",
    "**/R$*.class",
    "**/BuildConfig.*",
    "**/Manifest*.*",
    "android/**/*.*",
    "**/IInAppBillingService\$*.class"
  )

  private val parcelerGeneratedClasses = listOf(
    "**/*\$\$Parcelable.class"
  )

  private val daggerGeneratedClasses = listOf(
    "**/*_MembersInjector.class",
    "**/Dagger*Component.class",
    "**/Dagger*Component\$Builder.class",
    "**/*_Factory.class",
    "**/*Module_*.class",
    "**/*\$ViewInjector*.*",
    "**/*\$InjectAdapter.class",
    "**/*\$ModuleAdapter.class"
  )

  private  val databindingFilter = listOf(
    "**/databinding/*Binding.class",
    "**/databinding/*Binding*Impl.class",
    "**/databinding/*BindingImpl.class",
    "**/DataBinderMapperImpl.class",
    "**/V1CompatDataBinderMapperImpl*.class",
    "**/generated/**/*.class",
    "**/BR.class",
    "**/BR$*.class"
  )

  private val testsFilter = listOf(
    "**/*Test*.*",
    "**/src/test/java/**/*.*",
    "**/src/*Test/java/**/*.*",
    "**/src/test/kotlin/**/*.*",
    "**/src/*Test/kotlin/**/*.*"
  )

  private val glideGeneratedClasses = listOf(
    "**/GeneratedAppGlideModuleImpl.class",
    "**/GeneratedRequestManagerFactory.class"
  )

  private val roomGeneratedClasses = listOf("**/*_Impl*.class")

  private  val autoValueGeneratedClasses = listOf("**/*AutoValue_*.*")

  fun retrieve() = genericExcludes +
      androidGeneratedClasses +
      parcelerGeneratedClasses +
      daggerGeneratedClasses +
      databindingFilter +
      testsFilter +
      glideGeneratedClasses +
      roomGeneratedClasses +
      autoValueGeneratedClasses
}
