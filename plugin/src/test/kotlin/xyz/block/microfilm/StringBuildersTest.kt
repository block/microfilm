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

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.Test

class StringBuildersTest {
  @Test
  fun `appendCheckboxLine uses checkmark prefix for correct value`() {
    assertThat(
        buildString {
          appendCheckboxLine(
            isCorrect = true,
            correctValue = "correct",
            incorrectValue = "incorrect",
          )
        }
      )
      .isEqualTo("[✓] correct\n")
  }

  @Test
  fun `appendCheckboxLine uses x prefix for incorrect value`() {
    assertThat(
        buildString {
          appendCheckboxLine(
            isCorrect = false,
            correctValue = "correct",
            incorrectValue = "incorrect",
          )
        }
      )
      .isEqualTo("[✗] incorrect\n")
  }

  @Test
  fun `appendCorrectCheckboxLine uses checkmark prefix`() {
    assertThat(buildString { appendCorrectCheckboxLine(value = "correct") })
      .isEqualTo("[✓] correct\n")
  }

  @Test
  fun `appendIncorrectCheckboxLine uses x prefix`() {
    assertThat(buildString { appendIncorrectCheckboxLine(value = "incorrect") })
      .isEqualTo("[✗] incorrect\n")
  }
}
