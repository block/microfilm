package app.cash.microfilm

import java.io.File
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault(because = "This task modifies the source tree in place")
abstract class CompressTask : DefaultTask() {
  @get:Internal abstract val microfilmDirectory: DirectoryProperty

  @get:Internal abstract val resourcesDirectory: DirectoryProperty

  @TaskAction
  fun compress() {
    // Find the resource PNGs
    val resourcesDirectoryFile = resourcesDirectory.get().asFile
    val resourcesPngs =
      resourcesDirectoryFile
        .walk()
        .filter { it.isFile && it.extension.equals("png", ignoreCase = true) }
        .filter { !it.name.endsWith(".9.png", ignoreCase = true) }
        .filter { it.isInDrawableDirectory() }
        .toList()

    // Sweep the resource PNGs to the microfilm directory
    val microfilmDirectoryFile = microfilmDirectory.get().asFile
    resourcesPngs.forEach { resourcesPng ->
      val microfilmPng =
        File(
          microfilmDirectoryFile,
          resourcesPng
            .relativeTo(base = resourcesDirectoryFile)
            .path
            .replace(oldChar = '\\', newChar = '/'),
        )
      microfilmPng.parentFile?.mkdirs()
      resourcesPng.copyTo(target = microfilmPng, overwrite = true)
      resourcesPng.delete()
    }

    // TODO: compress the PNGs to WebP
  }

  companion object {
    private val DRAWABLE_DIRECTORY_PATTERN = Regex("^drawable(-.*)?$")

    private fun File.isInDrawableDirectory(): Boolean {
      return parentFile?.name?.let { DRAWABLE_DIRECTORY_PATTERN.matches(it) } == true
    }
  }
}
