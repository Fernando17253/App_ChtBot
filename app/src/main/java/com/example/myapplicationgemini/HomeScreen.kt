package com.example.myapplicationgemini

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(onNavigateToChat: () -> Unit) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    Image(
      painter = painterResource(R.drawable.university_logo),
      contentDescription = "Logo Universidad",
      modifier = Modifier.size(150.dp)
    )
    Spacer(modifier = Modifier.height(16.dp))

    Text("Nombre del Alumno:", fontWeight = FontWeight.Bold)
    Text("José Fernando Durán Villatoro")
    Spacer(modifier = Modifier.height(4.dp))
    Text("Matrícula: 123456")
    Spacer(modifier = Modifier.height(8.dp))
    Text("Carrera:", fontWeight = FontWeight.Bold)
    Text("Ingeniería en desarrollo de software")
    Spacer(modifier = Modifier.height(5.dp))
    Text("Materia:", fontWeight = FontWeight.Bold)
    Text("Programación para moviles 2")
    Spacer(modifier = Modifier.height(5.dp))
    Text("Grupo: 9B")
    Spacer(modifier = Modifier.height(16.dp))

    // Botón para acceder al repositorio
    val context = LocalContext.current
    Button(
      onClick = {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/enlace-proyecto"))
        context.startActivity(intent)
      },
      colors = ButtonDefaults.buttonColors(
        containerColor = Color(0xFF2196F3),  // Fondo oscuro (color típico de GitHub)
        contentColor = Color.White  // Color del contenido blanco
      )
    ) {
      // Imagen de GitHub
      Image(
        painter = painterResource(R.drawable.git),  // Reemplaza con tu imagen de GitHub
        contentDescription = "GitHub",
        modifier = Modifier.size(24.dp)  // Tamaño de la imagen
      )
      Spacer(modifier = Modifier.width(8.dp))  // Espacio entre la imagen y el texto
      Text("Visitar Repositorio")  // Texto del botón
    }

    Spacer(modifier = Modifier.height(16.dp))

    Button(onClick = onNavigateToChat,
      colors = ButtonDefaults.buttonColors(
        containerColor = Color(0xFF2196F3),  // Fondo azul
        contentColor = Color.White  // Color del texto blanco
      )
    ) {
      // Imagen de ícono (Asegúrate de tener un ícono en drawable)
      Image(
        painter = painterResource(R.drawable.botchat),  // Reemplaza con el nombre de tu imagen
        contentDescription = "Ir al Chat",
        modifier = Modifier.size(24.dp)  // Tamaño de la imagen
      )
      Spacer(modifier = Modifier.width(8.dp))  // Espacio entre la imagen y el texto
      Text("Ir al Chat")  // Texto del botón
    }
  }
}
