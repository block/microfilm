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

/**
 * A verifier that checks images against the image settings declared in the Gradle configuration.
 */
internal interface Verifier<T : ImageSettings> {
  /** Verifies the given image using the given settings . */
  fun verify(imageGroup: ImageGroup, imageSettings: T): Result

  sealed interface Result {
    /** The key of the [ImageGroup] that was verified. */
    val key: String

    /** Returned when an image passes verification. */
    data class Success(override val key: String) : Result

    /** Returned when an image fails verification for some reason. */
    sealed interface Failure : Result {
      val description: String
    }
  }
}
