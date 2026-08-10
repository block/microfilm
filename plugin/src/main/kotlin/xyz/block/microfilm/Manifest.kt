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

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okio.FileSystem
import okio.Path
import xyz.block.microfilm.Manifest.Compressor

@Serializable
internal data class Manifest(val entries: List<Entry> = emptyList()) {
  @Serializable
  data class Entry(
    val sourcePath: String,
    val sourceSha256: String,
    val compressedPath: String,
    val compressedSha256: String,
    val compressor: Compressor,
  )

  @Serializable
  data class Compressor(
    val name: String,
    val version: String,
    val lossless: Boolean,
    val compressionFactor: Int?,
    val compressionMethod: Int?,
    val metadata: String?,
  )

  internal fun toJson(): String =
    JSON_SERIALIZER.encodeToString(serializer = serializer(), value = this) + "\n"

  companion object {
    internal fun fromJson(json: String): Manifest =
      JSON_DESERIALIZER.decodeFromString(string = json)

    @OptIn(ExperimentalSerializationApi::class)
    private val JSON_SERIALIZER = Json {
      encodeDefaults = true
      explicitNulls = false
      prettyPrint = true
      prettyPrintIndent = "  "
    }

    private val JSON_DESERIALIZER = Json {
      explicitNulls = false
      ignoreUnknownKeys = true
    }
  }
}

internal fun ImageSettings.Compress.toCompressor(cwebpVersion: String) =
  Compressor(
    name = "cwebp",
    version = cwebpVersion,
    lossless = lossless,
    compressionFactor = compressionFactor,
    compressionMethod = compressionMethod,
    metadata = metadata?.toString(),
  )

/** Reads a manifest from disk, or returns an empty manifest if there isn't an existing file. */
internal fun FileSystem.readManifest(path: Path): Manifest =
  if (exists(path = path)) {
    read(file = path) { readUtf8() }.let { string -> Manifest.fromJson(json = string) }
  } else {
    Manifest()
  }

/** Writes the manifest to disk, or deletes the existing file if the manifest is empty. */
internal fun FileSystem.writeManifest(path: Path, manifest: Manifest) {
  if (manifest.entries.isEmpty()) {
    delete(path = path)
  } else {
    path.parent?.let { parent -> createDirectories(dir = parent) }
    write(file = path) { writeUtf8(manifest.toJson()) }
  }
}
