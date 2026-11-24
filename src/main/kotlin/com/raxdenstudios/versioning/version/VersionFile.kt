package com.raxdenstudios.versioning.version

import org.gradle.api.GradleException
import java.io.File
import java.io.StringReader
import java.io.StringWriter
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.nio.file.attribute.PosixFileAttributeView
import java.util.Properties

/**
 * Reads, validates and writes `version.properties` files (keys MAJOR, MINOR, PATCH, DEV). A
 * missing key counts as 0. Problems are reported as [GradleException]s that name the file.
 */
object VersionFile {

    private val versionKeys = VersionPart.entries.map { it.key }

    /**
     * Reads and validates [file]. Throws [GradleException] when the file is missing or invalid
     * (see [parse]).
     */
    fun read(file: File): Version {
        if (!file.isFile) throw missingFile(file.path)
        return parse(file.readText(), file.path)
    }

    /**
     * Parses the text of a properties file and validates the result. [location] only appears in
     * error messages. Throws [GradleException] for a non-integer value or an invalid version.
     */
    fun parse(content: String, location: String): Version {
        val properties = load(content)
        val version = Version(
            major = properties.intValue(VersionPart.MAJOR, location),
            minor = properties.intValue(VersionPart.MINOR, location),
            patch = properties.intValue(VersionPart.PATCH, location),
            dev = properties.intValue(VersionPart.DEV, location),
        )
        val errors = version.validationErrors()
        if (errors.isNotEmpty()) {
            throw GradleException("Invalid version in '$location': ${errors.joinToString("; ")}")
        }
        return version
    }

    /**
     * Writes [version] in a single atomic replace, keys in a fixed order and without a timestamp
     * comment. Keys other than the version ones already present in the file are kept at the end.
     */
    fun write(file: File, version: Version) {
        val extraEntries = if (file.isFile) load(file.readText()).extraEntries() else emptyList()
        val content = buildString {
            appendLine("${VersionPart.MAJOR.key}=${version.major}")
            appendLine("${VersionPart.MINOR.key}=${version.minor}")
            appendLine("${VersionPart.PATCH.key}=${version.patch}")
            appendLine("${VersionPart.DEV.key}=${version.dev}")
            extraEntries.forEach { (key, value) -> append(escapedEntry(key, value)) }
        }
        replaceAtomically(file, content)
    }

    /** Error for a version file that does not exist, pointing at the `filePath` setting. */
    fun missingFile(location: String): GradleException =
        GradleException("Version file '$location' does not exist. Check `versioning { filePath = ... }`.")

    private fun load(content: String): Properties =
        Properties().apply { load(StringReader(content)) }

    private fun Properties.intValue(part: VersionPart, location: String): Int {
        val raw = getProperty(part.key)?.trim() ?: return 0
        return raw.toIntOrNull()
            ?: throw GradleException("Invalid version in '$location': ${part.key} must be an integer (was '$raw')")
    }

    private fun Properties.extraEntries(): List<Pair<String, String>> =
        stringPropertyNames()
            .filterNot { it in versionKeys }
            .sorted()
            .map { it to getProperty(it) }

    /** `key=value` line(s) with standard `.properties` escaping, without the date comment. */
    private fun escapedEntry(key: String, value: String): String {
        val writer = StringWriter()
        Properties().apply { setProperty(key, value) }.store(writer, null)
        return writer.toString()
            .lineSequence()
            .filter { it.isNotEmpty() && !it.startsWith("#") }
            .joinToString(separator = "") { "$it\n" }
    }

    private fun replaceAtomically(file: File, content: String) {
        // Write through symlinks to the real file instead of replacing the link.
        val target = file.toPath().let { if (Files.exists(it)) it.toRealPath() else it.toAbsolutePath() }
        val temp = Files.createTempFile(target.parent, ".${target.fileName}", ".tmp")
        try {
            Files.writeString(temp, content)
            copyPermissions(from = target, to = temp)
            try {
                Files.move(temp, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)
            } catch (_: AtomicMoveNotSupportedException) {
                Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING)
            }
        } finally {
            Files.deleteIfExists(temp)
        }
    }

    /** createTempFile yields 0600: keep the original file's mode where POSIX permissions exist. */
    private fun copyPermissions(from: Path, to: Path) {
        if (!Files.exists(from)) return
        if (Files.getFileAttributeView(from, PosixFileAttributeView::class.java) == null) return
        Files.setPosixFilePermissions(to, Files.getPosixFilePermissions(from))
    }
}
