object ApplicationId {
  const val id = "com.rs.lib.preferences.sample"
}

object Modules {
  const val libraryPreferences = ":libraries:preferences"

  const val sample = ":sample"
}

object Releases {
  const val major = 0
  const val minor = 1   // Max 99
  const val patch = 1   // Max 99
  const val dev = 1     // Max 99

  const val versionCode = major * 1000000 + minor * 10000 + patch * 100 + dev
  const val versionName = "$major.$minor.$patch"
}

object Versions {
  const val minSdk = 21
  const val compileSdk = 29
  const val targetSdk = 29
  const val kotlin = "1.3.72"

  const val gson = "2.8.6"                      // https://github.com/google/gson
  const val preferences = "1.1.1"

  const val archCoreTest = "2.0.0"
  const val atsl = "1.1.0"

  const val robolectric = "4.3.1"
}

object KotlinLibraries {
  const val kotlin = "org.jetbrains.kotlin:kotlin-stdlib-jdk7:${Versions.kotlin}"
}

object AndroidLibraries {
  // ANDROID
  const val preferences = "androidx.preference:preference-ktx:${Versions.preferences}"
}

object Libraries {
  // Gson
  const val gson = "com.google.code.gson:gson:${Versions.gson}"
}

object TestLibraries {
  // ANDROID TEST
  const val archCoreTest = "androidx.arch.core:core-testing:${Versions.archCoreTest}"
  const val atslJunit = "androidx.test.ext:junit:${Versions.atsl}"
  const val atslRunner = "androidx.test:runner:${Versions.atsl}"
  const val atslRules = "androidx.test:rules:${Versions.atsl}"

  // Robolectric
  const val robolectric = "org.robolectric:robolectric:${Versions.robolectric}"
}