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
package xyz.block.microfilm.decompression

import okio.FileSystem
import okio.Path.Companion.toOkioPath
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import org.gradle.work.DisableCachingByDefault
import xyz.block.microfilm.Manifest
import xyz.block.microfilm.scanning.RealScanner
import xyz.block.microfilm.writeManifest

@DisableCachingByDefault(because = "This task modifies the source tree in place")
internal abstract class DecompressTask : DefaultTask() {
  @get:Internal
  @get:Option(
    option = "images",
    description =
      "Decompress the images matching these glob patterns, relative to the res directory.",
  )
  abstract val imagePatterns: ListProperty<String>

  @get:Internal abstract val microfilmDirectory: DirectoryProperty

  @get:Internal abstract val resourcesDirectory: DirectoryProperty

  private val resourcesDirectoryPath by lazy { resourcesDirectory.get().asFile.toOkioPath() }
  private val microfilmDirectoryPath by lazy { microfilmDirectory.get().asFile.toOkioPath() }
  private val microfilmManifestPath by lazy {
    microfilmDirectoryPath.resolve(child = "manifest.json")
  }

  @TaskAction
  fun decompress() {
    // Initialize the dependencies
    val fileSystem = FileSystem.SYSTEM
    val scanner =
      RealScanner(
        fileSystem = fileSystem,
        resourcesDirectory = resourcesDirectoryPath,
        microfilmDirectory = microfilmDirectoryPath,
      )
    val decompressor =
      RealDecompressor(
        fileSystem = fileSystem,
        resourcesDirectory = resourcesDirectoryPath,
        microfilmDirectory = microfilmDirectoryPath,
      )

    // Decompress the images
    val manifestEntries =
      decompressor.decompress(
        scanner = scanner,
        imagePatterns = imagePatterns.get().ifEmpty { listOf("**") },
        resourcesDirectory = resourcesDirectoryPath,
        microfilmDirectory = microfilmDirectoryPath,
      )

    // Create the manifest from remaining images
    val manifest = Manifest(entries = manifestEntries.sortedBy { entry -> entry.sourcePath })

    // Write the manifest to disk
    fileSystem.writeManifest(path = microfilmManifestPath, manifest = manifest)
  }
}
