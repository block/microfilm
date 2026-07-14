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
import kotlinx.serialization.json.Json
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
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

@DisableCachingByDefault(because = "This task produces no outputs")
internal abstract class VerifyTask @Inject constructor(private val execOperations: ExecOperations) :
  DefaultTask() {
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
  fun verify() {
    val cwebp = RealCwebp(execOperations = execOperations, directory = cwebpDirectory)
    val failures = mutableListOf<String>()

    // Collect the images in the manifest
    val manifest =
      if (microfilmManifestFile.exists()) {
        JSON.decodeFromString(Manifest.serializer(), microfilmManifestFile.readText())
      } else {
        Manifest()
      }
    val manifestSourcePaths = manifest.entries.map { entry -> entry.sourcePath }.toSet()
    val manifestCompressedPaths = manifest.entries.map { entry -> entry.compressedPath }.toSet()

    // Collect the PNG images in the microfilm directory
    val microfilmPngs = microfilmDirectoryFile.walk().filter { file -> file.isPngDrawable }
    val microfilmPngPaths =
      microfilmPngs
        .map { microfilmPng ->
          microfilmPng.relativeTo(base = microfilmDirectoryFile).invariantSeparatorsPath
        }
        .toSet()

    // Collect the PNG images in the resources directory
    val resourcesPngs = resourcesDirectoryFile.walk().filter { file -> file.isPngDrawable }
    val resourcesPngPaths =
      resourcesPngs
        .map { resourcesPng ->
          resourcesPng.relativeTo(base = microfilmDirectoryFile).invariantSeparatorsPath
        }
        .toSet()

    // Collect the WebP images in the resources directory
    val resourcesWebps = resourcesDirectoryFile.walk().filter { file -> file.isWebpDrawable }
    val resourcesWebpPaths =
      resourcesWebps
        .map { resourcesWebp ->
          resourcesWebp.relativeTo(base = resourcesDirectoryFile).invariantSeparatorsPath
        }
        .toSet()

    // Check the microfilm directory for unexpected PNGs
    failures.addAll(
      microfilmPngPaths.toSet().subtract(manifestSourcePaths).map { microfilmPngPath ->
        "Found unexpected PNG image in microfilm directory: $microfilmPngPath"
      }
    )

    // Check the microfilm directory for expected PNGs
    failures.addAll(
      manifestSourcePaths.subtract(microfilmPngPaths).map { manifestSourcePath ->
        "Missing expected PNG image in microfilm directory: $manifestSourcePath"
      }
    )

    // Check the microfilm PNG SHAs
    failures.addAll(
      microfilmPngs
        .mapNotNull { microfilmPng ->
          val relativePng = microfilmPng.relativeTo(base = microfilmDirectoryFile)
          manifest.entries
            .firstOrNull { entry -> entry.sourcePath == relativePng.invariantSeparatorsPath }
            ?.let { entry -> microfilmPng to entry }
        }
        .filterNot { (microfilmPng, entry) -> microfilmPng.sha256() == entry.sourceSha256 }
        .map { (_, entry) ->
          "Incorrect SHA for WebP image in resources directory: ${entry.sourcePath}"
        }
    )

    // Check the resources directory for unexpected PNGs
    failures.addAll(
      resourcesPngPaths
        .filter { resourcesPngPath -> resourcesPngPath.resolveImageSettings() is Compress }
        .map { resourcesPngPath ->
          "Found unexpected PNG image in resources directory: $resourcesPngPath"
        }
    )

    // Check the resources directory for expected WebPs
    failures.addAll(
      manifestCompressedPaths.subtract(resourcesWebpPaths).map { manifestCompressedPath ->
        "Missing expected WebP image in resources directory: $manifestCompressedPath"
      }
    )

    // Check the resources WebP SHAs
    failures.addAll(
      resourcesWebps
        .mapNotNull { resourcesWebp ->
          val relativePng = resourcesWebp.relativeTo(base = resourcesDirectoryFile)
          manifest.entries
            .firstOrNull { entry -> entry.compressedPath == relativePng.invariantSeparatorsPath }
            ?.let { entry -> resourcesWebp to entry }
        }
        .filterNot { (resourcesWebp, entry) -> resourcesWebp.sha256() == entry.compressedSha256 }
        .map { (_, entry) ->
          "Incorrect SHA for WebP image in resources directory: ${entry.compressedSha256}"
        }
    )

    // Check the compression settings
    val cwebpVersion = cwebp.getVersion()
    failures.addAll(
      manifest.entries
        .map { entry -> entry to entry.sourcePath.resolveImageSettings() }
        .filter { (entry, imageSettings) ->
          when (imageSettings) {
            is Compress ->
              entry.compressor != imageSettings.toCompressor(cwebpVersion = cwebpVersion)
            is Exclude -> true
          }
        }
        .map { (entry, _) ->
          "Incorrect compression settings for PNG image in microfilm directory: ${entry.sourcePath}"
        }
    )

    // Report any failures
    if (failures.isNotEmpty()) {
      throw GradleException(
        buildString {
          appendLine("Microfilm verification failed with ${failures.size} error(s):")
          failures.forEach { failure -> appendLine("  - $failure") }
          append("Run ./gradlew compressMicrofilm to update.")
        }
      )
    }
  }

  private fun String.resolveImageSettings(): ImageSettings =
    imageRules.get().resolve(imagePath = this)?.imageSettings ?: Exclude

  companion object {
    private val JSON = Json {
      explicitNulls = false
      ignoreUnknownKeys = true
    }
  }
}
