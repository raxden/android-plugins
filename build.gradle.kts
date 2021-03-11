plugins {
    id("com.gradle.plugin-publish") version "0.11.0"
    `kotlin-dsl`
    // Apply other plugins here, e.g. the kotlin plugin for a plugin written in Kotlin
    // or the groovy plugin if the plugin uses Groovy
}

// If your plugin has any external java dependencies, Gradle will attempt to
// download them from JCenter for anyone using the plugins DSL
// so you should probably use JCenter for dependency resolution in your own
// project.
repositories {
    google()
    jcenter()
}

// Use java-gradle-plugin to generate plugin descriptors and specify plugin ids
gradlePlugin {
    plugins {
        create("versioningPlugin") {
            id = "com.raxdenstudios.android-versioning"
            implementationClass = "com.raxdenstudios.versioning.VersioningPlugin"
        }
        create("releasingPlugin") {
            id = "com.raxdenstudios.android-releasing"
            implementationClass = "com.raxdenstudios.releasing.ReleasingPlugin"
        }
        create("publishLibraryPlugin") {
            id = "com.raxdenstudios.publish-library"
            implementationClass = "com.raxdenstudios.publishing.PublishLibraryPlugin"
        }
        create("jacocoPlugin") {
            id = "com.raxdenstudios.android-jacoco"
            implementationClass = "com.raxdenstudios.reporting.JacocoPlugin"
        }
    }
}


pluginBundle {
    // These settings are set for the whole plugin bundle
    website = "https://github.com/raxden/android-plugins"
    vcsUrl = "https://github.com/raxden/android-plugins.git"

    // tags and description can be set for the whole bundle here, but can also
    // be set / overridden in the config for specific plugins
    description = "Allow to include basic configuration for commons modules in Android."

    // The plugins block can contain multiple plugin entries.
    //
    // The name for each plugin block below (greetingsPlugin, goodbyePlugin)
    // does not affect the plugin configuration, but they need to be unique
    // for each plugin.

    // Plugin config blocks can set the id, displayName, version, description
    // and tags for each plugin.

    // id and displayName are mandatory.
    // If no version is set, the project version will be used.
    // If no tags or description are set, the tags or description from the
    // pluginBundle block will be used, but they must be set in one of the
    // two places.

    (plugins) {
        "versioningPlugin" {
            // id is captured from java-gradle-plugin configuration
            displayName = "Gradle Versioning plugin"
            tags = listOf("android")
            version = "0.39"
        }
        "releasingPlugin" {
            // id is captured from java-gradle-plugin configuration
            displayName = "Gradle Releasing plugin"
            tags = listOf("android")
            version = "0.39"
        }
        "publishLibraryPlugin" {
            // id is captured from java-gradle-plugin configuration
            displayName = "Gradle Publish plugin"
            tags = listOf("android")
            version = "0.39"
        }
        "jacocoPlugin" {
            // id is captured from java-gradle-plugin configuration
            displayName = "Gradle Jacoco plugin"
            tags = listOf("android")
            version = "0.39"
        }
    }

    // Optional overrides for Maven coordinates.
    // If you have an existing plugin deployed to Bintray and would like to keep
    // your existing group ID and artifact ID for continuity, you can specify
    // them here.
    //
    // As publishing to a custom group requires manual approval by the Gradle
    // team for security reasons, we recommend not overriding the group ID unless
    // you have an existing group ID that you wish to keep. If not overridden,
    // plugins will be published automatically without a manual approval process.
    //
    // You can also override the version of the deployed artifact here, though it
    // defaults to the project version, which would normally be sufficient.

    mavenCoordinates {
        groupId = "com.raxdenstudios"
        artifactId = "android-plugins"
        version = "0.39"
    }
}

// to publish -> gradlew publishPlugins

dependencies {
    /* Depend on the android gradle plugin, since we want to access it in our plugin */
    implementation("com.android.tools.build:gradle:4.1.1")
    implementation("org.ajoberstar.grgit:grgit-gradle:4.1.0")
    implementation("com.jfrog.bintray.gradle:gradle-bintray-plugin:1.8.4")

    /* Depend on the kotlin plugin, since we want to access it in our plugin */
    implementation(kotlin("gradle-plugin", version = "1.4.21"))

    /* Depend on the default Gradle API's since we want to build a custom plugin */
    implementation(gradleApi())
    implementation(localGroovy())
}
