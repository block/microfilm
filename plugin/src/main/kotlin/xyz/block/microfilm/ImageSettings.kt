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

import org.gradle.api.provider.Property

public sealed interface ImageSettings {
  public data class Compress(
    val lossless: Boolean = false,
    val compressionFactor: Int? = null,
    val compressionMethod: Int? = null,
    val metadata: ImageMetadata? = null,
  ) : ImageSettings {
    init {
      if (compressionFactor != null) {
        require(compressionFactor in 0..100) { "compressionFactor must be between 0 and 100" }
      }
      if (compressionMethod != null) {
        require(compressionMethod in 0..6) { "compressionMethod must be between 0 and 6" }
      }
    }

    public abstract class Spec {
      /**
       * Specifies whether the WebP compression is lossy or lossless. The default is false.
       *
       * See the cwebp documentation of the -lossless option for more info:
       * https://developers.google.com/speed/webp/docs/cwebp
       */
      public abstract val lossless: Property<Boolean>

      /**
       * Specifies the compression factor for RGB channels between 0 and 100. The default is 75.
       *
       * In case of lossy compression (default), a small factor produces a smaller file with lower
       * quality. Best quality is achieved by using a value of 100.
       *
       * In case of lossless compression (specified by the -lossless option), a small factor enables
       * faster compression speed, but produces a larger file. Maximum compression is achieved by
       * using a value of 100.
       *
       * See the cwebp documentation of the -q option for more info:
       * https://developers.google.com/speed/webp/docs/cwebp
       */
      public abstract val compressionFactor: Property<Int>

      /**
       * Specifies the compression method to use between 0 (fastest) and 6 (slowest). The default
       * is 4.
       *
       * See the cwebp documentation of the -m option for more info:
       * https://developers.google.com/speed/webp/docs/cwebp
       */
      public abstract val compressionMethod: Property<Int>

      /**
       * Specifies the metadata to copy from the source image to the compressed image if present.
       * The default is none.
       *
       * See the cwebp documentation of the -metadata option for more info:
       * https://developers.google.com/speed/webp/docs/cwebp
       */
      public abstract val metadata: Property<ImageMetadata>

      init {
        lossless.convention(false)
      }

      internal fun resolve(): Compress =
        Compress(
          lossless = lossless.get(),
          compressionFactor = compressionFactor.orNull,
          compressionMethod = compressionMethod.orNull,
          metadata = metadata.orNull,
        )
    }
  }

  public data object Exclude : ImageSettings
}
