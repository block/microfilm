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
package xyz.block.microfilm.cwebp

import okio.Path
import xyz.block.microfilm.ImageSettings.Compress

/** A wrapper around the cwebp executable. */
internal interface Cwebp {
  /** Returns the current cwebp version. */
  fun getVersion(): String

  /** Compresses the source PNG image to the destination WebP using the given settings. */
  fun compress(imageSettings: Compress, sourcePng: Path, destinationWebp: Path)
}
