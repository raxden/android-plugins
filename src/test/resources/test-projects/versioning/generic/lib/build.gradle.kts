plugins {
    id("com.android.library")
    id("com.raxdenstudios.android-versioning")
}

versioning {
    filePath = "version.properties"
}

android {
    namespace = "com.example.test"
    compileSdk = 34
    defaultConfig {
        minSdk = 24
    }
}