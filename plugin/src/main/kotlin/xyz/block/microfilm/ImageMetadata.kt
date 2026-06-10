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

/**
 * Metadata that can be copied from the source image to the compressed image if present.
 *
 * See the cwebp documentation of the -metadata option for more info:
 * https://developers.google.com/speed/webp/docs/cwebp
 */
public class ImageMetadata(
  public val exif: Boolean = false,
  public val icc: Boolean = false,
  public val xmp: Boolean = false,
) {
  override fun toString(): String =
    when {
      exif && icc && xmp -> "all"
      !exif && !icc && !xmp -> "none"
      else ->
        buildList {
            if (exif) add("exif")
            if (icc) add("icc")
            if (xmp) add("xmp")
          }
          .joinToString(separator = ",")
    }

  public companion object {
    /** The default value that copies no metadata from the source image to the compressed image. */
    public val None: ImageMetadata = ImageMetadata()
  }
}
