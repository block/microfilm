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
package xyz.block.microfilm.scanning

import kotlinx.serialization.json.Json
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import xyz.block.microfilm.Manifest
import xyz.block.microfilm.isPngDrawable
import xyz.block.microfilm.isWebpDrawable
import xyz.block.microfilm.listRecursivelyOrEmpty

/** A [Scanner] backed by an Okio [FileSystem]. */
internal class RealScanner(
  private val fileSystem: FileSystem,
  private val resourcesDirectory: Path,
  private val microfilmDirectory: Path,
) : Scanner {
  override fun scan(): List<ImageGroup> {
    // Read the png and webp images
    val resourcesPngs =
      fileSystem
        .listRecursivelyOrEmpty(dir = resourcesDirectory)
        .filter { path -> path.isPngDrawable }
        .filter { path -> fileSystem.metadata(path = path).isRegularFile }
        .associateBy { path -> path.relativeTo(other = resourcesDirectory).key }
    val resourcesWebps =
      fileSystem
        .listRecursivelyOrEmpty(dir = resourcesDirectory)
        .filter { path -> path.isWebpDrawable }
        .filter { path -> fileSystem.metadata(path = path).isRegularFile }
        .associateBy { path -> path.relativeTo(other = resourcesDirectory).key }
    val microfilmPngs =
      fileSystem
        .listRecursivelyOrEmpty(dir = microfilmDirectory)
        .filter { path -> path.isPngDrawable }
        .filter { path -> fileSystem.metadata(path = path).isRegularFile }
        .associateBy { path -> path.relativeTo(other = microfilmDirectory).key }

    // Read the manifest entries
    val microfilmManifestPath = microfilmDirectory.resolve("manifest.json")
    val microfilmManifestEntries =
      if (fileSystem.exists(path = microfilmManifestPath)) {
        fileSystem
          .read(file = microfilmManifestPath) { readUtf8() }
          .let<String, Manifest> { string -> JSON.decodeFromString(string = string) }
          .entries
          .associateBy { entry -> entry.sourcePath.toPath().key }
      } else {
        emptyMap()
      }

    // Group the images for each unique key
    return buildSet {
      addAll(resourcesPngs.keys)
      addAll(resourcesWebps.keys)
      addAll(microfilmPngs.keys)
      addAll(microfilmManifestEntries.keys)
    }
      .toList()
      .sorted()
      .map { key ->
        ImageGroup(
          key = key,
          resourcesPng = resourcesPngs[key],
          resourcesWebp = resourcesWebps[key],
          microfilmPng = microfilmPngs[key],
          microfilmManifestEntry = microfilmManifestEntries[key],
        )
      }
  }
}

private val JSON = Json {
  explicitNulls = false
  ignoreUnknownKeys = true
}

private val Path.key
  get() = buildList {
    addAll(segments.dropLast(n = 1))
    add(name.substringBeforeLast(delimiter = "."))
  }
    .joinToString(separator = "/")
