# android-plugins

Gradle plugins shared by the RaxdenStudios Android projects (`android-commons`,
`android-platform`, `android-components` and the apps). They are published to the
[Gradle Plugin Portal](https://plugins.gradle.org/search?term=com.raxdenstudios).

| Plugin id | Applies to | What it does |
|---|---|---|
| `com.raxdenstudios.android-versioning` | `com.android.application`, `com.android.dynamic-feature`, `com.android.library`, `java-platform` | Reads the version from a `version.properties` file and provides tasks to bump it |
| `com.raxdenstudios.android-publish-library` | `com.android.library` | Configures `maven-publish` + `signing` for the `release` variant (sources and javadoc jars) |
| `com.raxdenstudios.android-publish-platform` | `java-platform` | Configures `maven-publish` + `signing` for a BOM / Java platform |

The plugins compile against AGP 9.1 but do not bring AGP onto your classpath: apply
the Android Gradle Plugin yourself. Plugin order does not matter.

```kotlin
// settings.gradle.kts
pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}
```

## Versioning

```kotlin
plugins {
    id("com.android.application") // or com.android.library / java-platform
    id("com.raxdenstudios.android-versioning") version "<version>"
}

versioning {
    filePath = "version.properties" // relative to the module; default "version.properties"
}
```

`version.properties`:

```properties
MAJOR=1
MINOR=3
PATCH=4
DEV=2
```

- **Applications**: `versionName = MAJOR.MINOR.PATCH` and
  `versionCode = MAJOR * 1_000_000 + MINOR * 10_000 + PATCH * 100 + DEV`
  (`1.3.4` / `1030402` above) are set on every variant output.
- **Libraries and platforms**: `project.version = MAJOR.MINOR.PATCH`, which the
  publish plugins use when no explicit version is configured.
- Dynamic feature modules get the tasks but take their version from the base app.
- Missing keys count as `0`. `MINOR`, `PATCH` and `DEV` must be between 0 and 99,
  and the versionCode must not exceed Google Play's limit (2,100,000,000); invalid
  files fail the build with a message naming the file.

### Tasks (group `versioning`)

| Task | Effect |
|---|---|
| `getVersion` | Prints only the version (`./gradlew -q :app:getVersion`) — safe for scripts |
| `showVersionInfo` | Prints the file used, version name and (apps) version code |
| `increaseMajorVersion` | `MAJOR + 1`, resets `MINOR`, `PATCH`, `DEV` |
| `increaseMinorVersion` | `MINOR + 1`, resets `PATCH`, `DEV` |
| `increasePatchVersion` | `PATCH + 1`, resets `DEV` |
| `increaseDevVersion` | `DEV + 1` |

The file is rewritten in a fixed order (`MAJOR`, `MINOR`, `PATCH`, `DEV`) without a
timestamp comment. When several modules point at the same file, it is bumped only once
per build, so `./gradlew increasePatchVersion` without a module path is safe. All tasks
are compatible with the configuration cache, and a changed `version.properties`
invalidates it.

## Publishing

```kotlin
import com.raxdenstudios.publishing.model.Coordinates
import com.raxdenstudios.publishing.model.Developer

plugins {
    id("com.android.library")
    id("com.raxdenstudios.android-versioning") version "<version>"
    id("com.raxdenstudios.android-publish-library") version "<version>"
}

publishLibrary { // publishPlatform { ... } for the java-platform plugin
    name = "Android"
    description = "Android Core"
    url = "https://github.com/raxden/android-commons"
    developer = Developer(id = "jdoe", name = "Jane Doe", email = "jane@example.com")
    coordinates = Coordinates.default.copy(artifactId = "commons-core")
}
```

| Property | Default |
|---|---|
| `name` | project name |
| `description` | project description, else `Publication for <group>:<name>` |
| `url` | **required** |
| `developer` | **required** (or `pom.developers`) |
| `coordinates` | `groupId` = project group, `artifactId` = project name, `version` = project version |
| `pom` | Apache 2.0 license; SCM derived from `url` |
| `repository` | none — a Maven repository is added only when its `url` is set |
| `signing` | read from `SIGNING_KEY`, `SIGNING_PASSWORD`, `SIGNING_KEY_ID` |

The SCM block is derived from `url`: `https://github.com/owner/repo` gives
`scm:git:https://github.com/owner/repo.git` and
`scm:git:ssh://git@github.com/owner/repo.git`.

### Signing

Keys are taken from the `signing` property, then from the `SIGNING_KEY`,
`SIGNING_PASSWORD` and optional `SIGNING_KEY_ID` Gradle properties or environment
variables (ASCII-armored in-memory key).

- `publishToMavenLocal` works without keys (artifacts are not signed).
- Publishing to any remote repository (including NMCP / Maven Central Portal) fails
  fast if no key is configured.

### Maven Central

The RaxdenStudios libraries publish to the Maven Central Portal with
[NMCP](https://github.com/GradleUp/nmcp), which picks up the publications created by
these plugins; no `repository` is needed. The old OSSRH endpoint
(`Repository.sonatype`) was shut down in June 2025 and is deprecated.

For any other Maven repository (import the model classes explicitly: a bare
`Credentials` resolves to Gradle's own `org.gradle.api.credentials.Credentials`):

```kotlin
import com.raxdenstudios.publishing.model.Credentials
import com.raxdenstudios.publishing.model.Repository

publishLibrary {
    repository = Repository(
        name = "internal",
        url = "https://maven.example.com/releases",
        credentials = Credentials.default, // falls back to OSSRH_USERNAME / OSSRH_PASSWORD
    )
}
```

## Development

```bash
./gradlew build validatePlugins   # compile, TestKit tests, plugin validation
./gradlew publishPlugins          # publish to the Gradle Plugin Portal
```

The TestKit fixtures build Android projects, so an Android SDK must be available
(`ANDROID_HOME` or `local.properties`). The plugin version lives in `pluginVersion`
in `build.gradle.kts`.
