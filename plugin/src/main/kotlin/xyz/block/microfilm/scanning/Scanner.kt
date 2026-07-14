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
package xyz.block.microfilm.scanning

/**
 * A scanner that locates image files on disk and groups them by key.
 *
 * The key is the image's path relative to the `res` or `microfilm` directory, without the file
 * extension. So for example, these three files would all share the key `drawable-hdpi/image`:
 * - `src/main/res/drawable-hdpi/image.png`
 * - `src/main/res/drawable-hdpi/image.webp`
 * - `src/main/microfilm/drawable-hdpi/image.png`
 */
internal interface Scanner {
  /** Returns a list of images on disk grouped by key. */
  fun scan(): List<ImageGroup>
}
