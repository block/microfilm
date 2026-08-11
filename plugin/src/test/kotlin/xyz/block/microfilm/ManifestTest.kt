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

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import okio.fakefilesystem.FakeFileSystem
import org.junit.jupiter.api.Test
import xyz.block.microfilm.scanning.ImageGroupFixtures.LOSSLESS_MANIFEST_ENTRY
import xyz.block.microfilm.scanning.ImageGroupFixtures.LOSSY_MANIFEST_ENTRY
import xyz.block.microfilm.scanning.ImageGroupFixtures.MICROFILM_DIRECTORY

class ManifestTest {
  private val fileSystem = FakeFileSystem()

  @Test
  fun `toJson serializes Manifest`() {
    assertThat(LOSSLESS_MANIFEST.toJson()).isEqualTo(LOSSLESS_JSON)
  }

  @Test
  fun `fromJson deserializes Manifest`() {
    assertThat(Manifest.fromJson(json = LOSSLESS_JSON)).isEqualTo(LOSSLESS_MANIFEST)
  }

  @Test
  fun `readManifest without existing file returns empty manifest`() {
    val manifest = fileSystem.readManifest(path = MANIFEST_PATH)

    assertThat(manifest).isEqualTo(EMPTY_MANIFEST)
  }

  @Test
  fun `readManifest with existing file returns valid manifest`() {
    fileSystem.writeManifest(path = MANIFEST_PATH, manifest = LOSSLESS_MANIFEST)

    val manifest = fileSystem.readManifest(path = MANIFEST_PATH)

    assertThat(manifest).isEqualTo(LOSSLESS_MANIFEST)
  }

  @Test
  fun `writeManifest empty manifest without existing file does nothing`() {
    fileSystem.writeManifest(path = MANIFEST_PATH, manifest = EMPTY_MANIFEST)

    assertThat(fileSystem.exists(path = MANIFEST_PATH)).isFalse()
  }

  @Test
  fun `writeManifest empty manifest with existing file deletes existing file`() {
    fileSystem.writeManifest(path = MANIFEST_PATH, manifest = LOSSLESS_MANIFEST)

    fileSystem.writeManifest(path = MANIFEST_PATH, manifest = EMPTY_MANIFEST)

    assertThat(fileSystem.exists(path = MANIFEST_PATH)).isFalse()
  }

  @Test
  fun `writeManifest valid manifest without existing file writes new file`() {
    fileSystem.writeManifest(path = MANIFEST_PATH, manifest = LOSSLESS_MANIFEST)

    assertThat(fileSystem.exists(path = MANIFEST_PATH)).isTrue()
    assertThat(fileSystem.readManifest(path = MANIFEST_PATH)).isEqualTo(LOSSLESS_MANIFEST)
  }

  @Test
  fun `writeManifest valid manifest with existing file overwrites existing file`() {
    fileSystem.writeManifest(path = MANIFEST_PATH, manifest = LOSSY_MANIFEST)

    fileSystem.writeManifest(path = MANIFEST_PATH, manifest = LOSSLESS_MANIFEST)

    assertThat(fileSystem.exists(path = MANIFEST_PATH)).isTrue()
    assertThat(fileSystem.readManifest(path = MANIFEST_PATH)).isEqualTo(LOSSLESS_MANIFEST)
  }

  companion object {
    val MANIFEST_PATH = MICROFILM_DIRECTORY.resolve(child = "manifest.json")

    private val EMPTY_MANIFEST = Manifest()
    private val LOSSLESS_MANIFEST = Manifest(entries = listOf(LOSSLESS_MANIFEST_ENTRY))
    private val LOSSY_MANIFEST = Manifest(entries = listOf(LOSSY_MANIFEST_ENTRY))

    private val LOSSLESS_JSON =
      """
      {
        "entries": [
          {
            "sourcePath": "drawable/photo.png",
            "sourceSha256": "8f8cbb7dcf46e0bc7d53265749a6c17d116093a6ba95e442764060c76fd4a86c",
            "compressedPath": "drawable/photo.webp",
            "compressedSha256": "a57bb082e728a0cdce930ecfcccf4510a3a247be5f322b09b3a971a3f5ed34f8",
            "compressor": {
              "name": "cwebp",
              "version": "1.2.3",
              "lossless": true
            }
          }
        ]
      }

      """
        .trimIndent()
  }
}
