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
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault
import xyz.block.microfilm.scanning.RealScanner

@DisableCachingByDefault(because = "This task modifies the source tree in place")
internal abstract class DecompressTask : DefaultTask() {
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
    scanner.scan().forEach { imageGroup ->
      decompressor.decompress(imageGroup = imageGroup)
    }

    // Delete the manifest
    fileSystem.delete(path = microfilmManifestPath)
  }
}
