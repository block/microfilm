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

import xyz.block.microfilm.ImageSettings
import xyz.block.microfilm.ImageSettings.Compress
import xyz.block.microfilm.ImageSettings.Exclude
import xyz.block.microfilm.ImageSettings.Unspecified
import xyz.block.microfilm.compression.Compressor.Result
import xyz.block.microfilm.scanning.ImageGroup

internal class TypedCompressor(
  private val compressCompressor: Compressor<Compress>,
  private val excludeCompressor: Compressor<Exclude>,
  private val unspecifiedCompressor: Compressor<Unspecified>,
) : Compressor<ImageSettings> {
  override fun compress(imageGroup: ImageGroup, imageSettings: ImageSettings): Result {
    return when (imageSettings) {
      is Compress ->
        compressCompressor.compress(imageGroup = imageGroup, imageSettings = imageSettings)

      is Exclude ->
        excludeCompressor.compress(imageGroup = imageGroup, imageSettings = imageSettings)

      is Unspecified ->
        unspecifiedCompressor.compress(imageGroup = imageGroup, imageSettings = imageSettings)
    }
  }
}
