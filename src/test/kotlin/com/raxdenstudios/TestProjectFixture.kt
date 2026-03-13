package com.raxdenstudios

import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

object TestProjectFixture {

    fun copyFixture(fixturePath: String, destinationDir: Path) {
        val resourceUrl = requireNotNull(
            TestProjectFixture::class.java.classLoader.getResource(fixturePath)
        ) {
            "Fixture not found: $fixturePath"
        }

        val source = File(resourceUrl.toURI())
        source.copyRecursively(destinationDir.toFile(), overwrite = true)
    }

    private fun File.copyRecursively(target: File, overwrite: Boolean) {
        if (isDirectory) {
            if (!target.exists()) target.mkdirs()
            listFiles().orEmpty().forEach { child ->
                child.copyRecursively(File(target, child.name), overwrite)
            }
        } else {
            target.parentFile?.mkdirs()
            Files.copy(
                toPath(),
                target.toPath(),
                if (overwrite) StandardCopyOption.REPLACE_EXISTING else StandardCopyOption.COPY_ATTRIBUTES
            )
        }
    }
}