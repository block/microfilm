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
package xyz.block.microfilm.compression

import javax.inject.Inject
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okio.FileSystem
import okio.Path.Companion.toOkioPath
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
import xyz.block.microfilm.ImageRule
import xyz.block.microfilm.Manifest
import xyz.block.microfilm.compression.Compressor.Result.Failure
import xyz.block.microfilm.compression.Compressor.Result.Success
import xyz.block.microfilm.cwebp.RealCwebp
import xyz.block.microfilm.scanning.RealScanner

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

  private val resourcesDirectoryPath by lazy { resourcesDirectory.get().asFile.toOkioPath() }
  private val microfilmDirectoryPath by lazy { microfilmDirectory.get().asFile.toOkioPath() }
  private val microfilmManifestPath by lazy {
    microfilmDirectoryPath.resolve(child = "manifest.json")
  }

  @TaskAction
  fun compress() {
    // Initialize the dependencies
    val cwebp = RealCwebp(execOperations = execOperations, directory = cwebpDirectory)
    val fileSystem = FileSystem.SYSTEM
    val compressor =
      TypedCompressor(
        compressCompressor =
          CompressCompressor(
            cwebp = cwebp,
            fileSystem = fileSystem,
            resourcesDirectory = resourcesDirectoryPath,
            microfilmDirectory = microfilmDirectoryPath,
          ),
        excludeCompressor =
          ExcludeCompressor(
            fileSystem = fileSystem,
            resourcesDirectory = resourcesDirectoryPath,
            microfilmDirectory = microfilmDirectoryPath,
          ),
        unspecifiedCompressor =
          UnspecifiedCompressor(
            fileSystem = fileSystem,
            resourcesDirectory = resourcesDirectoryPath,
            microfilmDirectory = microfilmDirectoryPath,
          ),
      )
    val scanner =
      RealScanner(
        fileSystem = fileSystem,
        resourcesDirectory = resourcesDirectoryPath,
        microfilmDirectory = microfilmDirectoryPath,
      )

    // Compress the images
    val results =
      compressor.compress(
        scanner = scanner,
        imageRules = imageRules.get(),
        resourcesDirectory = resourcesDirectoryPath,
        microfilmDirectory = microfilmDirectoryPath,
      )

    // Create the manifest
    val manifest =
      Manifest(
        entries =
          results
            .filterIsInstance<Success>()
            .mapNotNull { success -> success.microfilmManifestEntry }
            .sortedBy { entry -> entry.sourcePath }
      )

    // Write the manifest to disk
    if (manifest.entries.isEmpty()) {
      fileSystem.delete(path = microfilmManifestPath)
    } else {
      fileSystem.createDirectories(dir = microfilmDirectoryPath)
      fileSystem.write(file = microfilmManifestPath) {
        writeUtf8(JSON.encodeToString(serializer = Manifest.serializer(), value = manifest) + "\n")
      }
    }

    // Fail if any images could not be compressed
    val failures = results.filterIsInstance<Failure>()
    if (failures.isNotEmpty()) {
      throw GradleException(
        buildString {
          appendLine("Microfilm compression failed for ${failures.size} image(s):")
          appendLine(failures.joinToString(separator = "\n\n") { failure -> failure.description })
        }
      )
    }
  }

  companion object {
    @OptIn(ExperimentalSerializationApi::class)
    private val JSON = Json {
      encodeDefaults = true
      explicitNulls = false
      prettyPrint = true
      prettyPrintIndent = "  "
    }
  }
}
