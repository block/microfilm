/*
 * Copyright (C) 2026 Block, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package xyz.block.microfilm

import java.io.File
import javax.inject.Inject
import kotlin.text.RegexOption.IGNORE_CASE
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okio.Path.Companion.toOkioPath
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
import xyz.block.microfilm.cwebp.RealCwebp

@DisableCachingByDefault(because = "This task modifies the source tree in place")
internal abstract class CompressTask
@Inject
constructor(private val execOperations: ExecOperations) : DefaultTask() {
  @get:InputFiles
  @get:PathSensitive(RELATIVE)
  abstract val cwebpDirectory: ConfigurableFileCollection

  @get:Internal abstract val imageRules: ListProperty<ImageRule>

  @get:Internal abstract val microfilmDirectory: DirectoryProperty

  @get:Internal abstract val resourcesDirectory: DirectoryProperty

  private val microfilmDirectoryFile by lazy { microfilmDirectory.get().asFile }
  private val microfilmManifestFile by lazy { File(microfilmDirectoryFile, "manifest.json") }
  private val resourcesDirectoryFile by lazy { resourcesDirectory.get().asFile }

  @TaskAction
  fun compress() {
    val cwebp = RealCwebp(execOperations = execOperations, directory = cwebpDirectory)

    // Find the resources PNGs
    val resourcesPngs = resourcesDirectoryFile.walk().filter { file -> file.isPngDrawable }.toList()

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
    val microfilmPngs = microfilmDirectoryFile.walk().filter { file -> file.isPngDrawable }.toList()

    // Compress the microfilm PNGs to WebP in the resources directory
    val cwebpVersion = cwebp.getVersion()
    microfilmPngs
      .mapNotNull { microfilmPng ->
        val imageSettings =
          microfilmPng.relativeTo(base = microfilmDirectoryFile).resolveImageSettings()
        if (imageSettings is Compress) microfilmPng to imageSettings else null
      }
      .forEach { (microfilmPng, imageSettings) ->
        val resourcesWebp = microfilmPngToResourcesWebp(microfilmPng = microfilmPng)
        resourcesWebp.parentFile?.mkdirs()
        cwebp.compress(
          imageSettings = imageSettings,
          sourcePng = microfilmPng.toOkioPath(),
          destinationWebp = resourcesWebp.toOkioPath(),
        )
      }

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
                compressor = imageSettings.toCompressor(cwebpVersion = cwebpVersion),
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
    return imageRules.get().resolve(imagePath = invariantSeparatorsPath)?.imageSettings ?: Exclude
  }

  companion object {
    private val PNG_EXTENSION_PATTERN = Regex(pattern = "\\.png$", option = IGNORE_CASE)

    @OptIn(ExperimentalSerializationApi::class)
    private val JSON = Json {
      encodeDefaults = true
      explicitNulls = false
      prettyPrint = true
      prettyPrintIndent = "  "
    }
  }
}
