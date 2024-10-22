package com.example.myapplicationgemini

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController


class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      MainScreen()
    }
  }
}

@Composable
fun MainScreen() {
  val navController = rememberNavController()
  var selectedIndex by remember { mutableStateOf(0) }

  Scaffold(
    bottomBar = {
      BottomNavigationBar(
        selectedIndex = selectedIndex,
        onItemSelected = { index ->
          selectedIndex = index
          when (index) {
            0 -> navController.navigate("home")
            1 -> navController.navigate("chat")
            2 -> navController.navigate("geolocator")
            3 -> navController.navigate("qrflutter")
            4 -> navController.navigate("sensorplus")
          }
        }
      )
    }
  ) { paddingValues ->
    NavigationHost(navController = navController, modifier = Modifier.padding(paddingValues))
  }
}

@Composable
fun BottomNavigationBar(selectedIndex: Int, onItemSelected: (Int) -> Unit) {
  NavigationBar(
    containerColor = Color.White,
    contentColor = MaterialTheme.colorScheme.primary
  ) {
    NavigationBarItem(
      icon = { Icon(Icons.Default.Person, contentDescription = "Perfil") },
      label = { Text("Perfil") },
      selected = selectedIndex == 0,
      onClick = { onItemSelected(0) }
    )
    NavigationBarItem(
      icon = { Icon(painterResource(R.drawable.botchat1), contentDescription = "Chat") },  // Ícono personalizado
      label = { Text("Chat") },
      selected = selectedIndex == 1,
      onClick = { onItemSelected(1) }
    )
    NavigationBarItem(
      icon = { Icon(Icons.Default.LocationOn, contentDescription = "Geolocalización") },
      label = { Text("GPS") },
      selected = selectedIndex == 2,
      onClick = { onItemSelected(2) }
    )
    NavigationBarItem(
      icon = { Icon(painterResource(R.drawable.qrscan), contentDescription = "QR Scanner") },
      label = { Text("QR") },
      selected = selectedIndex == 3,
      onClick = { onItemSelected(3) }
    )
    NavigationBarItem(
      icon = { Icon(painterResource(R.drawable.sensor), contentDescription = "Sensores") },
      label = { Text("Sensores") },
      selected = selectedIndex == 4,
      onClick = { onItemSelected(4) }
    )
  }
}

@Composable
fun NavigationHost(navController: NavHostController, modifier: Modifier) {
  NavHost(navController = navController, startDestination = "home", modifier = modifier) {
    composable("home") { HomeScreen(onNavigateToChat = { navController.navigate("chat") }) }
    composable("chat") { BakingScreen(onNavigateBack = { navController.navigate("home") }) }
    composable("geolocator") { GeoLocatorScreen() }
    composable("qrflutter") { QRScannerScreen() }
    composable("sensorplus") { SensorPlusScreen() }
  }
}
