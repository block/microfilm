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

import java.io.Serializable
import java.nio.file.FileSystems
import java.nio.file.Path

internal data class ImageRule(val pattern: String, val imageSettings: ImageSettings) : Serializable

/** Returns true if the [ImageRule] matches the given image. */
internal fun ImageRule.matches(imagePath: String): Boolean =
  FileSystems.getDefault().getPathMatcher("glob:$pattern").matches(Path.of(imagePath))

/** Returns the last [ImageRule] that matches the given image, or null if none match. */
internal fun List<ImageRule>.resolve(imagePath: String): ImageRule? = lastOrNull { imageRule ->
  imageRule.matches(imagePath)
}

/** Returns the last [ImageRule] that matches the given image, or null if none match. */
internal fun List<ImageRule>.resolve(imagePath: okio.Path): ImageRule? =
  resolve(imagePath = imagePath.toString())
