package xyz.block.microfilm

import org.gradle.api.provider.Property

data class ImageSettings(val lossless: Boolean, val compressionFactor: Int?) {
  init {
    if (compressionFactor != null) {
      require(compressionFactor in 0..100) { "compressionFactor must be between 0 and 100" }
    }
  }

  abstract class Spec {
    /**
     * Specifies whether the WebP compression is lossy or lossless. The default is false.
     *
     * See the cwebp documentation of the -lossless option for more info:
     * https://developers.google.com/speed/webp/docs/cwebp
     */
    abstract val lossless: Property<Boolean>

    /**
     * Specify the compression factor for RGB channels between 0 and 100. The default is 75.
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
    abstract val compressionFactor: Property<Int>

    init {
      lossless.convention(false)
    }

    internal fun resolve(): ImageSettings {
      return ImageSettings(lossless = lossless.get(), compressionFactor = compressionFactor.orNull)
    }
  }
}
