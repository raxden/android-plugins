plugins {
    id("com.android.application")
    id("com.raxdenstudios.android-versioning")
}

versioning {
    filePath = "../version.properties"
}

android {
    namespace = "com.example.test"
    compileSdk = 34
    defaultConfig {
        applicationId = "com.example.test"
        minSdk = 24
        targetSdk = 34
    }
}