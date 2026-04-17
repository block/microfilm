package app.cash.microfilm

import java.io.File
import javax.inject.Inject
import kotlin.text.RegexOption.IGNORE_CASE
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity.RELATIVE
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault(because = "This task modifies the source tree in place")
abstract class CompressTask @Inject constructor(private val execOperations: ExecOperations) :
  DefaultTask() {
  @get:InputFiles
  @get:PathSensitive(RELATIVE)
  abstract val cwebpDirectory: ConfigurableFileCollection

  @get:Internal abstract val microfilmDirectory: DirectoryProperty

  @get:Internal abstract val resourcesDirectory: DirectoryProperty

  @get:Internal abstract val lossless: Property<Boolean>

  @get:Internal abstract val quality: Property<Int>

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

    // Compress the PNGs to WebP in the resources directory
    val cwebpExecutable = cwebpDirectory.singleFile.resolve("cwebp")
    microfilmDirectoryFile
      .walk()
      .filter { it.isFile && it.extension.equals("png", ignoreCase = true) }
      .toList()
      .forEach { microfilmPng ->
        val resourcesWebp =
          File(
            resourcesDirectoryFile,
            microfilmPng
              .relativeTo(base = microfilmDirectoryFile)
              .path
              .replace(oldChar = '\\', newChar = '/')
              .replace(PNG_EXTENSION_PATTERN, ".webp"),
          )
        resourcesWebp.parentFile?.mkdirs()
        execOperations.exec { action ->
          action.commandLine(
            buildList {
              add(cwebpExecutable.absolutePath)
              add("-metadata")
              add("icc")
              if (lossless.get()) {
                add("-lossless")
              } else {
                add("-q")
                add(quality.get().toString())
              }
              add("-o")
              add(resourcesWebp.absolutePath)
              add(microfilmPng.absolutePath)
            }
          )
        }
      }
  }

  companion object {
    private val DRAWABLE_DIRECTORY_PATTERN = Regex(pattern = "^drawable(-.*)?$")
    private val PNG_EXTENSION_PATTERN = Regex(pattern = "\\.png$", option = IGNORE_CASE)

    private fun File.isInDrawableDirectory(): Boolean {
      return parentFile?.name?.let { DRAWABLE_DIRECTORY_PATTERN.matches(it) } == true
    }
  }
}
