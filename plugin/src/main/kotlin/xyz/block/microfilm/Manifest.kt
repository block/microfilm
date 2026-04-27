package xyz.block.microfilm

import kotlinx.serialization.Serializable

@Serializable
data class Manifest(val entries: List<Entry> = emptyList()) {
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
    val quality: Int?,
  )
}
