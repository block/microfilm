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

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import org.junit.jupiter.api.Test
import xyz.block.microfilm.ImageSettings.Compress
import xyz.block.microfilm.ImageSettings.Exclude
import xyz.block.microfilm.ImageSettings.Unspecified
import xyz.block.microfilm.scanning.ImageGroupFixtures.LOSSY_COMPRESS
import xyz.block.microfilm.scanning.ImageGroupFixtures.LOSSY_IMAGE_GROUP

class TypedVerifierTest {
  private val compressVerifier = FakeVerifier<Compress>()
  private val excludeVerifier = FakeVerifier<Exclude>()
  private val unspecifiedVerifier = FakeVerifier<Unspecified>()

  private val verifier =
    TypedVerifier(
      compressVerifier = compressVerifier,
      excludeVerifier = excludeVerifier,
      unspecifiedVerifier = unspecifiedVerifier,
    )

  @Test
  fun `verify delegates to compress verifier`() {
    verifier.verify(imageGroup = LOSSY_IMAGE_GROUP, imageSettings = LOSSY_COMPRESS)

    assertThat(compressVerifier.verifyRequests).containsExactly(LOSSY_IMAGE_GROUP to LOSSY_COMPRESS)
    assertThat(excludeVerifier.verifyRequests).isEmpty()
    assertThat(unspecifiedVerifier.verifyRequests).isEmpty()
  }

  @Test
  fun `verify delegates to exclude verifier`() {
    verifier.verify(imageGroup = LOSSY_IMAGE_GROUP, imageSettings = Exclude)

    assertThat(compressVerifier.verifyRequests).isEmpty()
    assertThat(excludeVerifier.verifyRequests).containsExactly(LOSSY_IMAGE_GROUP to Exclude)
    assertThat(unspecifiedVerifier.verifyRequests).isEmpty()
  }

  @Test
  fun `verify delegates to unspecified verifier`() {
    verifier.verify(imageGroup = LOSSY_IMAGE_GROUP, imageSettings = Unspecified)

    assertThat(compressVerifier.verifyRequests).isEmpty()
    assertThat(excludeVerifier.verifyRequests).isEmpty()
    assertThat(unspecifiedVerifier.verifyRequests).containsExactly(LOSSY_IMAGE_GROUP to Unspecified)
  }
}
