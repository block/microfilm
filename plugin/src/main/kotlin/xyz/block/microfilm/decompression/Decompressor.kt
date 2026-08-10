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

import okio.Path
import okio.Path.Companion.toPath
import xyz.block.microfilm.Manifest
import xyz.block.microfilm.matchesGlobs
import xyz.block.microfilm.scanning.ImageGroup
import xyz.block.microfilm.scanning.Scanner

/**
 * A decompressor that removes compressed images and restores their original uncompressed
 * counterparts.
 */
internal interface Decompressor {
  /** Removes the given compressed image and restores its original uncompressed counterpart. */
  fun decompress(imageGroup: ImageGroup)
}

/** Scans for images and decompresses them. */
internal fun Decompressor.decompress(
  scanner: Scanner,
  imagePatterns: List<String>,
  resourcesDirectory: Path,
  microfilmDirectory: Path,
): List<Manifest.Entry> {
  return scanner.scan().mapNotNull { imageGroup ->
    val pngPath =
      imageGroup.microfilmManifestEntry?.sourcePath?.toPath()
        ?: imageGroup.microfilmPng?.relativeTo(other = microfilmDirectory)
        ?: imageGroup.resourcesPng?.relativeTo(other = resourcesDirectory)
    val webpPath = imageGroup.resourcesWebp?.relativeTo(other = resourcesDirectory)
    if (listOfNotNull(pngPath, webpPath).matchesGlobs(patterns = imagePatterns)) {
      decompress(imageGroup = imageGroup)
      null
    } else {
      imageGroup.microfilmManifestEntry
    }
  }
}
