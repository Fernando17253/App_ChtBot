package com.example.myapplicationgemini

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Task
import com.google.android.gms.location.LocationResult
import kotlinx.coroutines.launch
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.location.Priority
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import android.location.Location
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Warning

@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun GeoLocatorScreen() {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var locationStatus by remember { mutableStateOf("Desconocido") }
    var lastPosition by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    val scope = rememberCoroutineScope()

    // Solicitar permisos de ubicación
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted: Boolean ->
            if (isGranted) {
                // El permiso está otorgado
                scope.launch {
                    getCurrentLocation(fusedLocationClient, onLocationResult = { latitude, longitude ->
                        locationStatus = "Ubicación REAL: $latitude, $longitude"
                        lastPosition = Pair(latitude, longitude)
                    })
                }
            } else {
                // El permiso fue denegado
                locationStatus = "Permiso de ubicación denegado"
            }
        }
    )

    // Solicitar permiso de ubicación si no está otorgado
    val permissionStatus = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
    if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
        // Obtener la ubicación actual si ya tenemos el permiso
        scope.launch {
            getCurrentLocation(fusedLocationClient, onLocationResult = { latitude, longitude ->
                locationStatus = "Ubicación REAL: $latitude, $longitude"
                lastPosition = Pair(latitude, longitude)
            })
        }
    } else {
        // Solicitar permisos de ubicación
        LaunchedEffect(Unit) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    // Abrir Google Maps con la última ubicación
    fun openInGoogleMaps() {
        if (lastPosition != null) {
            val url = "https://www.google.com/maps/search/?api=1&query=${lastPosition!!.first},${lastPosition!!.second}"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
            } else {
                Toast.makeText(context, "No se pudo abrir Google Maps", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // UI para mostrar la ubicación y los botones de acción
    Box(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F0F0)),
            elevation = CardDefaults.cardElevation(8.dp),
            content = {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = if (locationStatus.contains("FALSA")) Icons.Default.Warning else Icons.Default.LocationOn,
                        contentDescription = "Ubicación",
                        tint = if (locationStatus.contains("FALSA")) Color.Red else Color.Green,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = locationStatus,
                        style = MaterialTheme.typography.titleLarge,
                        color = if (locationStatus.contains("FALSA")) Color.Red else Color.Black
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = {
                        scope.launch {
                            getCurrentLocation(fusedLocationClient, onLocationResult = { latitude, longitude ->
                                locationStatus = "Ubicación REAL: $latitude, $longitude"
                                lastPosition = Pair(latitude, longitude)
                            })
                        }
                    }) {
                        Text("Actualizar Ubicación")
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { openInGoogleMaps() }, enabled = lastPosition != null) {
                        Text("Ver en Google Maps")
                    }
                }
            }
        )
    }
}

// Función para obtener la ubicación actual
@SuppressLint("MissingPermission")
suspend fun getCurrentLocation(
    fusedLocationClient: FusedLocationProviderClient,
    onLocationResult: (Double, Double) -> Unit
) {
    val cancellationTokenSource = CancellationTokenSource()

    try {
        val locationTask: Task<Location> = fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            cancellationTokenSource.token
        )

        locationTask.addOnSuccessListener { location ->
            location?.let {
                onLocationResult(it.latitude, it.longitude)
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
