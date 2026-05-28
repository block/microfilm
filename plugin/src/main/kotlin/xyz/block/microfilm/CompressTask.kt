package xyz.block.microfilm

import java.io.ByteArrayOutputStream
import java.io.File
import javax.inject.Inject
import kotlin.text.RegexOption.IGNORE_CASE
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity.RELATIVE
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import org.gradle.work.DisableCachingByDefault
import xyz.block.microfilm.ImageSettings.Compress
import xyz.block.microfilm.ImageSettings.Exclude

@DisableCachingByDefault(because = "This task modifies the source tree in place")
abstract class CompressTask @Inject constructor(private val execOperations: ExecOperations) :
  DefaultTask() {
  @get:InputFiles
  @get:PathSensitive(RELATIVE)
  abstract val cwebpDirectory: ConfigurableFileCollection

  @get:Internal abstract val microfilmDirectory: DirectoryProperty

  @get:Internal abstract val resourcesDirectory: DirectoryProperty

  @get:Internal abstract val rules: ListProperty<ImageRule>

  private val microfilmDirectoryFile by lazy { microfilmDirectory.get().asFile }
  private val microfilmManifestFile by lazy { File(microfilmDirectoryFile, "manifest.json") }
  private val resourcesDirectoryFile by lazy { resourcesDirectory.get().asFile }

  @TaskAction
  fun compress() {
    // Find the resources PNGs
    val resourcesPngs =
      resourcesDirectoryFile
        .walk()
        .filter { it.isFile && it.extension.equals("png", ignoreCase = true) }
        .filter { !it.name.endsWith(".9.png", ignoreCase = true) }
        .filter { it.isInDrawableDirectory() }
        .toList()

    // Sweep the resources PNGs to the microfilm directory
    resourcesPngs
      .filter { resourcesPng ->
        resourcesPng.relativeTo(base = resourcesDirectoryFile).resolveImageSettings() is Compress
      }
      .forEach { resourcesPng ->
        val microfilmPng = resourcesPngToMicrofilmPng(resourcesPng = resourcesPng)
        microfilmPng.parentFile?.mkdirs()
        resourcesPng.copyTo(target = microfilmPng, overwrite = true)
        resourcesPng.delete()
      }

    // Find the microfilm PNGs
    val microfilmPngs =
      microfilmDirectoryFile
        .walk()
        .filter { it.isFile && it.extension.equals("png", ignoreCase = true) }
        .toList()

    // Compress the microfilm PNGs to WebP in the resources directory
    val cwebpExecutable = cwebpDirectory.singleFile.resolve("cwebp")
    microfilmPngs
      .mapNotNull { microfilmPng ->
        val imageSettings =
          microfilmPng.relativeTo(base = microfilmDirectoryFile).resolveImageSettings()
        if (imageSettings is Compress) microfilmPng to imageSettings else null
      }
      .forEach { (microfilmPng, imageSettings) ->
        val resourcesWebp = microfilmPngToResourcesWebp(microfilmPng = microfilmPng)
        resourcesWebp.parentFile?.mkdirs()
        execOperations.exec { action ->
          action.commandLine(
            buildList {
              add(cwebpExecutable.absolutePath)
              add("-metadata")
              add("icc")
              if (imageSettings.lossless) {
                add("-lossless")
              }
              imageSettings.compressionFactor?.let { compressionFactor ->
                add("-q")
                add(compressionFactor.toString())
              }
              add("-o")
              add(resourcesWebp.absolutePath)
              add(microfilmPng.absolutePath)
            }
          )
        }
      }

    // Get the current cwebp version
    val output = ByteArrayOutputStream()
    execOperations.exec { action ->
      action.commandLine(cwebpExecutable.absolutePath, "-version")
      action.standardOutput = output
    }
    val cwebpVersion = output.toString().lines().first().trim()

    // Create the manifest
    val manifest =
      Manifest(
        entries =
          microfilmPngs
            .sortedBy { microfilmPng -> microfilmPng.invariantSeparatorsPath }
            .mapNotNull { microfilmPng ->
              val imageSettings =
                microfilmPng.relativeTo(base = microfilmDirectoryFile).resolveImageSettings()
              if (imageSettings is Compress) microfilmPng to imageSettings else null
            }
            .map { (microfilmPng, imageSettings) ->
              val resourcesWebp = microfilmPngToResourcesWebp(microfilmPng = microfilmPng)
              Manifest.Entry(
                sourcePath =
                  microfilmPng.relativeTo(base = microfilmDirectoryFile).invariantSeparatorsPath,
                sourceSha256 = microfilmPng.sha256(),
                compressedPath =
                  resourcesWebp.relativeTo(base = resourcesDirectoryFile).invariantSeparatorsPath,
                compressedSha256 = resourcesWebp.sha256(),
                compressor =
                  Manifest.Compressor(
                    name = "cwebp",
                    version = cwebpVersion,
                    lossless = imageSettings.lossless,
                    compressionFactor = imageSettings.compressionFactor,
                  ),
              )
            }
      )

    // Write the manifest to disk
    if (manifest.entries.isEmpty()) {
      microfilmManifestFile.delete()
    } else {
      microfilmManifestFile.writeText(
        text = JSON.encodeToString(serializer = Manifest.serializer(), value = manifest) + "\n"
      )
    }
  }

  private fun resourcesPngToMicrofilmPng(resourcesPng: File) =
    File(
      microfilmDirectoryFile,
      resourcesPng.relativeTo(base = resourcesDirectoryFile).invariantSeparatorsPath,
    )

  private fun microfilmPngToResourcesWebp(microfilmPng: File) =
    File(
      resourcesDirectoryFile,
      microfilmPng
        .relativeTo(base = microfilmDirectoryFile)
        .invariantSeparatorsPath
        .replace(PNG_EXTENSION_PATTERN, ".webp"),
    )

  private fun File.resolveImageSettings(): ImageSettings {
    return rules.get().resolve(imagePath = invariantSeparatorsPath)?.imageSettings ?: Exclude
  }

  companion object {
    private val DRAWABLE_DIRECTORY_PATTERN = Regex(pattern = "^drawable(-.*)?$")
    private val PNG_EXTENSION_PATTERN = Regex(pattern = "\\.png$", option = IGNORE_CASE)

    @OptIn(ExperimentalSerializationApi::class)
    private val JSON = Json {
      encodeDefaults = true
      prettyPrint = true
      prettyPrintIndent = "  "
    }

    private fun File.isInDrawableDirectory(): Boolean {
      return parentFile?.name?.let { DRAWABLE_DIRECTORY_PATTERN.matches(it) } == true
    }
  }
}
