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
import okio.Path
import xyz.block.microfilm.scanning.ImageGroup

internal class RealDecompressor(
  private val fileSystem: FileSystem,
  private val resourcesDirectory: Path,
  private val microfilmDirectory: Path,
) : Decompressor {
  override fun decompress(imageGroup: ImageGroup) {
    var resourcesPng = imageGroup.resourcesPng
    val resourcesWebp = imageGroup.resourcesWebp
    val microfilmPng = imageGroup.microfilmPng

    // If there's a microfilm png, move it back to the resources directory
    if (microfilmPng != null) {
      val relativePng = microfilmPng.relativeTo(other = microfilmDirectory)
      resourcesPng = resourcesDirectory.resolve(child = relativePng)
      resourcesPng.parent?.let { parent -> fileSystem.createDirectories(dir = parent) }
      fileSystem.atomicMove(source = microfilmPng, target = resourcesPng)
    }

    // If there's a resources png and a resources webp, delete the webp
    if (resourcesPng != null && resourcesWebp != null) {
      fileSystem.delete(path = resourcesWebp)
    }
  }
}
