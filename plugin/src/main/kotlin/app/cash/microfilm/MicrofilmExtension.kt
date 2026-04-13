package app.cash.microfilm

import org.gradle.api.provider.Property

abstract class MicrofilmExtension {
  /** Specifies whether the WebP compression is lossless or lossy. */
  abstract val lossless: Property<Boolean>

  /** Specifies the WebP compression quality (0-100). Ignored when [lossless] is true. */
  abstract val quality: Property<Int>
}
