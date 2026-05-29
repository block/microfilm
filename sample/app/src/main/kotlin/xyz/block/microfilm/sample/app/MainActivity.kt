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
package xyz.block.microfilm.sample.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import xyz.block.microfilm.sample.app.R as AppR
import xyz.block.microfilm.sample.library.R as LibraryR

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      MaterialTheme {
        Column(
          modifier = Modifier.fillMaxSize(),
          horizontalAlignment = CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(space = 16.dp, alignment = CenterVertically),
        ) {
          Text(text = "App Module", style = MaterialTheme.typography.h6)
          Image(
            modifier = Modifier.size(size = 120.dp),
            painter = painterResource(AppR.drawable.cash_app_green_lossless),
            contentDescription = "App module sample image",
          )

          Text(text = "Library Module", style = MaterialTheme.typography.h6)
          Image(
            modifier = Modifier.size(size = 120.dp),
            painter = painterResource(LibraryR.drawable.cash_app_black_lossy),
            contentDescription = "Library module sample image",
          )
        }
      }
    }
  }
}
