plugins {
    `kotlin-dsl`
    id("com.gradle.plugin-publish") version "2.0.0" // https://plugins.gradle.org/docs/publish-plugin
}

// to publish -> gradlew publishPlugins

val pluginVersion = "0.57.30"

group = "com.raxdenstudios"
version = pluginVersion

val agpDependency = "com.android.tools.build:gradle:9.1.0"

/*
 * AGP is applied by consumers, so it is compileOnly here to avoid forcing its version onto their
 * buildscript classpath. TestKit fixtures still need it: it is injected only into the
 * plugin-under-test classpath used by GradleRunner.withPluginClasspath().
 */
val testPluginClasspath: Configuration by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
}

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
            version = pluginVersion
        }
        create("publishLibraryPlugin") {
            id = "com.raxdenstudios.android-publish-library"
            implementationClass = "com.raxdenstudios.publishing.PublishLibraryPlugin"
            displayName = "Gradle Publish Plugin"
            description = "Gradle plugin to publish Android libraries to Maven Central."
            tags.set(listOf("publishing", "maven", "android", "library", "aar"))
            version = pluginVersion
        }
        create("publishPlatformPlugin") {
            id = "com.raxdenstudios.android-publish-platform"
            implementationClass = "com.raxdenstudios.publishing.PublishPlatformPlugin"
            displayName = "Gradle Publish Java Platform Plugin"
            description = "Gradle plugin to publish Java Platform/BOM modules with Maven Publish and Signing."
            tags.set(listOf("publishing", "maven", "bom", "platform", "java-platform"))
            version = pluginVersion
        }
    }
}

tasks.pluginUnderTestMetadata {
    pluginClasspath.from(testPluginClasspath)
}

tasks.test {
    useJUnitPlatform()
}

dependencies {
    compileOnly(agpDependency)
    testPluginClasspath(agpDependency)

    testImplementation(gradleTestKit())
    testImplementation(kotlin("test"))
    testImplementation(platform("org.junit:junit-bom:5.14.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    jvmToolchain(17)
}
