plugins {
    `kotlin-dsl`
    id("groovy")
    id("com.gradle.plugin-publish") version "2.0.0" // https://plugins.gradle.org/docs/publish-plugin
}

group = "com.raxdenstudios"
version = "1.0.0-SNAPSHOT"

// to publish -> gradlew publishPlugins

gradlePlugin {

    website = "https://github.com/raxden/android-plugins"
    vcsUrl = "https://github.com/raxden/android-plugins.git"

    plugins {
        create("versioningPlugin") {
            id = "com.raxdenstudios.android-versioning"
            implementationClass = "com.raxdenstudios.versioning.VersioningPlugin"
            displayName = "Gradle Versioning Plugin"
            description = "Gradle plugin to manage versioning of Android projects."
            tags.set(listOf("version", "android", "library"))
            version = "0.57.30"
        }
        create("publishLibraryPlugin") {
            id = "com.raxdenstudios.android-publish-library"
            implementationClass = "com.raxdenstudios.publishing.PublishLibraryPlugin"
            displayName = "Gradle Publish Plugin"
            description = "Gradle plugin to publish Android libraries to Maven Central."
            tags.set(listOf("publishing", "maven", "android", "library", "aar"))
            version = "0.57.30"
        }
        create("publishPlatformPlugin") {
            id = "com.raxdenstudios.android-publish-platform"
            implementationClass = "com.raxdenstudios.publishing.PublishPlatformPlugin"
            displayName = "Gradle Publish Java Platform Plugin"
            description = "Gradle plugin to publish Java Platform/BOM modules with Maven Publish and Signing."
            tags.set(listOf("publishing", "maven", "bom", "platform", "java-platform"))
            version = "0.57.30"
        }
    }
}

tasks.test {
    useJUnitPlatform()
}

// to publish -> gradlew publishPlugins

dependencies {
    /* Depend on the android gradle plugin, since we want to access it in our plugin */
    implementation("com.android.tools.build:gradle:9.1.0")

    /* Depend on the kotlin plugin, since we want to access it in our plugin */
    implementation(kotlin("gradle-plugin", version = "2.3.10"))

    testImplementation(gradleTestKit())
    testImplementation(kotlin("test"))
    testImplementation(platform("org.junit:junit-bom:5.10.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    /* Depend on the default Gradle API's since we want to build a custom plugin */
//    implementation(gradleApi())
//    implementation(localGroovy())
}

kotlin {
    jvmToolchain(17)
}