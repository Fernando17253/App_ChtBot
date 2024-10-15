package com.example.myapplicationgemini

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      MainActivityContent()
    }
  }
}

@Composable
fun MainActivityContent() {
  var showHomeScreen by rememberSaveable { mutableStateOf(true) }

  if (showHomeScreen) {
    HomeScreen(onNavigateToChat = { showHomeScreen = false })
  } else {
    BakingScreen(onNavigateBack = { showHomeScreen = true })  // Pasamos la función de navegar atrás
  }
}
