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
package xyz.block.microfilm.verification

import xyz.block.microfilm.ImageSettings
import xyz.block.microfilm.ImageSettings.Compress
import xyz.block.microfilm.ImageSettings.Exclude
import xyz.block.microfilm.ImageSettings.Unspecified
import xyz.block.microfilm.scanning.ImageGroup
import xyz.block.microfilm.verification.Verifier.Result

/** A [Verifier] that delegates to individual verifiers for each subclass of [ImageSettings]. */
internal class TypedVerifier(
  private val compressVerifier: Verifier<Compress>,
  private val excludeVerifier: Verifier<Exclude>,
  private val unspecifiedVerifier: Verifier<Unspecified>,
) : Verifier<ImageSettings> {
  override fun verify(imageGroup: ImageGroup, imageSettings: ImageSettings): Result {
    return when (imageSettings) {
      is Compress -> compressVerifier.verify(imageGroup = imageGroup, imageSettings = imageSettings)
      is Exclude -> excludeVerifier.verify(imageGroup = imageGroup, imageSettings = imageSettings)
      is Unspecified ->
        unspecifiedVerifier.verify(imageGroup = imageGroup, imageSettings = imageSettings)
    }
  }
}
