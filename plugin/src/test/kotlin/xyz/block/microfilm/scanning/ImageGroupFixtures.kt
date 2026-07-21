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

import okio.Path.Companion.toPath
import xyz.block.microfilm.ImageSettings.Compress
import xyz.block.microfilm.Manifest
import xyz.block.microfilm.cwebp.FakeCwebp
import xyz.block.microfilm.toCompressor

internal object ImageGroupFixtures {
  const val KEY = "drawable/photo"
  const val RELATIVE_PNG = "$KEY.png"
  const val RELATIVE_WEBP = "$KEY.webp"

  val PROJECT_DIRECTORY = "/project/src/main".toPath()
  val RESOURCES_DIRECTORY = PROJECT_DIRECTORY.resolve(child = "res")
  val MICROFILM_DIRECTORY = PROJECT_DIRECTORY.resolve(child = "microfilm")

  val RESOURCES_PNG = RESOURCES_DIRECTORY.resolve(child = RELATIVE_PNG)
  val RESOURCES_WEBP = RESOURCES_DIRECTORY.resolve(child = RELATIVE_WEBP)
  val MICROFILM_PNG = MICROFILM_DIRECTORY.resolve(child = RELATIVE_PNG)

  const val PNG_CONTENT = "png"
  const val PNG_HASH = "8f8cbb7dcf46e0bc7d53265749a6c17d116093a6ba95e442764060c76fd4a86c"
  const val WEBP_CONTENT = "webp"
  const val WEBP_HASH = "a57bb082e728a0cdce930ecfcccf4510a3a247be5f322b09b3a971a3f5ed34f8"

  val LOSSY_COMPRESS = Compress(lossless = false)
  val LOSSLESS_COMPRESS = Compress(lossless = true)

  val LOSSY_MANIFEST_ENTRY =
    Manifest.Entry(
      sourcePath = RELATIVE_PNG,
      sourceSha256 = PNG_HASH,
      compressedPath = RELATIVE_WEBP,
      compressedSha256 = WEBP_HASH,
      compressor = LOSSY_COMPRESS.toCompressor(cwebpVersion = FakeCwebp.VERSION),
    )
  val LOSSLESS_MANIFEST_ENTRY =
    Manifest.Entry(
      sourcePath = RELATIVE_PNG,
      sourceSha256 = PNG_HASH,
      compressedPath = RELATIVE_WEBP,
      compressedSha256 = WEBP_HASH,
      compressor = LOSSLESS_COMPRESS.toCompressor(cwebpVersion = FakeCwebp.VERSION),
    )

  val EMPTY_IMAGE_GROUP =
    ImageGroup(
      key = KEY,
      resourcesPng = null,
      resourcesWebp = null,
      microfilmPng = null,
      microfilmManifestEntry = null,
    )
  val LOSSY_IMAGE_GROUP =
    ImageGroup(
      key = KEY,
      resourcesPng = null,
      resourcesWebp = RESOURCES_WEBP,
      microfilmPng = MICROFILM_PNG,
      microfilmManifestEntry = LOSSY_MANIFEST_ENTRY,
    )
  val LOSSLESS_IMAGE_GROUP =
    ImageGroup(
      key = KEY,
      resourcesPng = null,
      resourcesWebp = RESOURCES_WEBP,
      microfilmPng = MICROFILM_PNG,
      microfilmManifestEntry = LOSSLESS_MANIFEST_ENTRY,
    )
}
