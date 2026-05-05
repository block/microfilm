package xyz.block.microfilm

import org.gradle.api.provider.Property

data class CompressionSettings(val lossless: Boolean, val quality: Int?) {
  init {
    if (quality != null) {
      require(quality in 0..100) { "quality must be between 0 and 100" }
    }
  }

  abstract class Spec {
    /** Specifies whether the WebP compression is lossless or lossy. */
    abstract val lossless: Property<Boolean>

    /** Specifies the WebP compression quality (0-100). Must not be set when [lossless] is true. */
    abstract val quality: Property<Int>

    init {
      lossless.convention(true)
    }

    internal fun resolve(): CompressionSettings {
      val resolvedLossless = lossless.get()
      val resolvedQuality = quality.orNull
      return if (resolvedLossless) {
        CompressionSettings(lossless = true, quality = null)
      } else {
        CompressionSettings(lossless = false, quality = resolvedQuality ?: DEFAULT_QUALITY)
      }
    }
  }

  companion object {
    private const val DEFAULT_QUALITY = 90
  }
}
