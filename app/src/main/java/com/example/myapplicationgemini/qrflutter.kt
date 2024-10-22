package com.example.myapplicationgemini

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions

@Composable
fun QRScannerScreen() {
    val context = LocalContext.current
    var qrContent by remember { mutableStateOf<String?>(null) }
    var isScanning by remember { mutableStateOf(false) }

    // Iniciar el escaneo del código QR
    val qrScannerLauncher = rememberLauncherForActivityResult(
        contract = ScanContract(),
        onResult = { result ->
            if (result.contents != null) {
                qrContent = result.contents
                handleQrContent(context, result.contents)
            } else {
                Toast.makeText(context, "Escaneo cancelado", Toast.LENGTH_SHORT).show()
            }
        }
    )

    fun startQrScan() {
        val options = ScanOptions().apply {
            setPrompt("Escanea un código QR")
            setBeepEnabled(false)
            setOrientationLocked(true)
        }
        qrScannerLauncher.launch(options)
        isScanning = true
    }

    // UI para mostrar el botón de escanear y el resultado del QR
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = { startQrScan() }) {
            Text("Escanear Código QR")
        }
        Spacer(modifier = Modifier.height(16.dp))
        qrContent?.let {
            Text("Contenido escaneado: $it", style = MaterialTheme.typography.bodyLarge)
        }
    }
}

// Función para manejar el contenido del QR
fun handleQrContent(context: Context, qrContent: String) {
    if (isValidUrl(qrContent)) {
        // Si es una URL válida, abrir en el navegador
        openInBrowser(context, qrContent)
    } else {
        // Si no es una URL, mostrar en un diálogo
        showQrContentDialog(context, qrContent)
    }
}

// Verificar si el contenido del QR es una URL válida
fun isValidUrl(url: String): Boolean {
    return Uri.parse(url).scheme in listOf("http", "https")
}

// Abrir la URL en el navegador
fun openInBrowser(context: Context, url: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    if (intent.resolveActivity(context.packageManager) != null) {
        context.startActivity(intent)
    } else {
        Toast.makeText(context, "No se pudo abrir la URL", Toast.LENGTH_SHORT).show()
    }
}

// Mostrar un diálogo con el contenido escaneado
fun showQrContentDialog(context: Context, qrContent: String) {
    Toast.makeText(context, "Contenido del QR: $qrContent", Toast.LENGTH_LONG).show()
}
