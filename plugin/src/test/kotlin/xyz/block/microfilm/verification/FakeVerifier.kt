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
import xyz.block.microfilm.scanning.ImageGroup
import xyz.block.microfilm.verification.Verifier.Result
import xyz.block.microfilm.verification.Verifier.Result.Success

internal class FakeVerifier<T : ImageSettings> : Verifier<T> {
  val verifyRequests = ArrayDeque<Pair<ImageGroup, ImageSettings>>()
  val verifyResponses = ArrayDeque<Result>()

  override fun verify(imageGroup: ImageGroup, imageSettings: T): Result {
    verifyRequests.add(imageGroup to imageSettings)
    return verifyResponses.removeFirstOrNull() ?: Success(key = imageGroup.key)
  }
}
