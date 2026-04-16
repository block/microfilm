package app.cash.microfilm.sample.app

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
import app.cash.microfilm.sample.app.R as AppR
import app.cash.microfilm.sample.library.R as LibraryR

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
            painter = painterResource(AppR.drawable.cash_app_green),
            contentDescription = "App module sample image",
          )

          Text(text = "Library Module", style = MaterialTheme.typography.h6)
          Image(
            modifier = Modifier.size(size = 120.dp),
            painter = painterResource(LibraryR.drawable.cash_app_black),
            contentDescription = "Library module sample image",
          )
        }
      }
    }
  }
}
