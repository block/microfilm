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

import kotlin.text.RegexOption.IGNORE_CASE
import okio.Path
import okio.Path.Companion.toPath

/** Matches Android drawable resource directories like `drawable` and `drawable-hdpi`. */
private val DRAWABLE_DIRECTORY_PATTERN = Regex(pattern = "^drawable(-.*)?$")

/** True if this is a file in a drawable directory. */
internal val Path.isInDrawableDirectory
  get() = parent?.name?.matches(regex = DRAWABLE_DIRECTORY_PATTERN) == true

/**
 * True if this is a PNG image in a drawable directory.
 *
 * Excludes nine-patch (`.9.png`) images because Android treats them differently than regular PNGs
 * and they cannot be converted to any other image format.
 */
internal val Path.isPngDrawable
  get() =
    name.endsWith(suffix = ".png", ignoreCase = true) &&
      !name.endsWith(suffix = ".9.png", ignoreCase = true) &&
      isInDrawableDirectory

/** True if this is a WebP image in a drawable directory. */
internal val Path.isWebpDrawable
  get() = isInDrawableDirectory && name.endsWith(suffix = ".webp", ignoreCase = true)

/** Returns a new path with the new file extension instead of the old one. */
internal fun Path.replaceExtension(old: String, new: String) =
  toString()
    .replace(regex = Regex(pattern = "\\.$old$", option = IGNORE_CASE), replacement = ".$new")
    .toPath()
