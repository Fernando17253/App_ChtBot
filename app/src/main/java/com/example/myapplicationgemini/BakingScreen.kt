package com.example.myapplicationgemini

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.Color
import java.time.format.DateTimeFormatter

@Composable
fun BakingScreen(
  onNavigateBack: () -> Unit,
  bakingViewModel: BakingViewModel = viewModel()
) {
  val placeholderPrompt = stringResource(R.string.prompt_placeholder)
  var prompt by rememberSaveable { mutableStateOf(placeholderPrompt) }
  val uiState by bakingViewModel.uiState.collectAsState()
  val context = LocalContext.current
  var isRecording by rememberSaveable { mutableStateOf(false) }

  val speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
  val recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "es-ES")
    putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 5000L)
    putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 4000L)
    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
  }

  speechRecognizer.setRecognitionListener(object : RecognitionListener {
    override fun onReadyForSpeech(params: Bundle?) {}
    override fun onBeginningOfSpeech() {}
    override fun onRmsChanged(rmsdB: Float) {}
    override fun onBufferReceived(buffer: ByteArray?) {}
    override fun onEndOfSpeech() {}

    override fun onError(error: Int) {
      Log.e("SpeechRecognizer", "Error en la grabación: $error")
      Toast.makeText(context, "Error en la grabación: $error", Toast.LENGTH_SHORT).show()
    }

    override fun onResults(results: Bundle?) {
      val spokenTextResult = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()
      spokenTextResult?.let {
        prompt = it
        bakingViewModel.sendPrompt(it)
        prompt = ""
      }
    }

    override fun onPartialResults(partialResults: Bundle?) {}
    override fun onEvent(eventType: Int, params: Bundle?) {}
  })

  Box(
    modifier = Modifier.fillMaxSize()
  ) {
    Row(
      modifier = Modifier
        .align(Alignment.TopCenter)
        .fillMaxWidth()
        .padding(16.dp)
    ) {
      IconButton(onClick = { onNavigateBack() }) {
        Image(
          painter = painterResource(R.drawable.ic_back_arrow),
          contentDescription = "Regresar a HomeScreen"
        )
      }

      Spacer(modifier = Modifier.width(8.dp))

      Text(
        text = stringResource(R.string.baking_title),
        style = MaterialTheme.typography.titleLarge,
      )

      Spacer(modifier = Modifier.weight(1f))

      IconButton(onClick = { bakingViewModel.clearHistory() }) {
        Image(
          painter = painterResource(R.drawable.ic_delete),
          contentDescription = "Limpiar Historial"
        )
      }
    }

    if (uiState is UiState.Loading) {
      CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
    } else if (bakingViewModel.history.isEmpty()) {
      Text(
        text = "No hay preguntas hechas.",
        modifier = Modifier.align(Alignment.Center),
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurface
      )
    } else {
      val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")

      LazyColumn(
        modifier = Modifier
          .fillMaxSize()
          .padding(top = 70.dp, bottom = 105.dp)
      ) {
        items(bakingViewModel.history) { item ->
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(8.dp)
          ) {
            Card(
              modifier = Modifier
                .fillMaxWidth(0.75f)
                .align(Alignment.End),
              colors = CardDefaults.cardColors(
                containerColor = Color(0xFFBBDEFB)
              )
            ) {
              Column(modifier = Modifier.padding(8.dp)) {
                Text(
                  text = "Pregunta: ${item.question}",
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.primary
                )
                Text(
                  text = item.timestamp.format(formatter),
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurface
                )
              }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Card(
              modifier = Modifier
                .fillMaxWidth(0.75f)
                .align(Alignment.Start),
              colors = CardDefaults.cardColors(
                containerColor = Color(0xFFC8E6C9)
              )
            ) {
              Column(modifier = Modifier.padding(8.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(
                    text = "Respuesta: ${item.answer}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                  )

                  // Botón para reproducir la respuesta en audio
                  IconButton(
                    onClick = { bakingViewModel.speakText(item.answer) }
                  ) {
                    Icon(
                      painter = painterResource(R.drawable.ic_audio), // Icono para reproducir audio
                      contentDescription = "Reproducir respuesta"
                    )
                  }

                  // Botón de detener
                  IconButton(
                    onClick = { bakingViewModel.stopText() }
                  ) {
                    Icon(
                      painter = painterResource(R.drawable.ic_stop),
                      contentDescription = "Detener respuesta"
                    )
                  }

                }

                Text(
                  text = item.timestamp.format(formatter),
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurface
                )
              }
            }
          }
        }
      }
    }


    Row(
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .fillMaxWidth()
        .padding(16.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      TextField(
        value = prompt,
        label = { Text(stringResource(R.string.label_prompt)) },
        onValueChange = { prompt = it },
        modifier = Modifier
          .weight(1f)
          .height(80.dp)
          .padding(end = 16.dp)
      )

      IconButton(
        onClick = {
          bakingViewModel.sendPrompt(prompt)
          prompt = ""
        },
        enabled = prompt.isNotEmpty() && isWifiConnected()
      ) {
        Icon(
          painter = painterResource(R.drawable.send),
          contentDescription = "Enviar mensaje",
          modifier = Modifier.size(33.dp),
          tint = if (prompt.isNotEmpty() && isWifiConnected()) MaterialTheme.colorScheme.primary else Color.Gray  // Cambia el color según el estado del texto y Wi-Fi
        )
      }

      IconButton(
        onClick = {
          isRecording = !isRecording
          if (isRecording) {
            speechRecognizer.startListening(recognizerIntent)
            Toast.makeText(context, "Grabando...", Toast.LENGTH_SHORT).show()
          } else {
            speechRecognizer.stopListening()
            Toast.makeText(context, "Grabación detenida", Toast.LENGTH_SHORT).show()
          }
        },
        enabled = isWifiConnected()
      ) {
        Icon(
          painter = painterResource(R.drawable.mic),  // Ícono del micrófono
          contentDescription = if (isRecording) "Detener grabación" else "Grabar",
          modifier = Modifier.size(40.dp),  // Tamaño del ícono
          tint = if (isRecording) Color.Green else if (isWifiConnected()) MaterialTheme.colorScheme.primary else Color.Gray
        )
      }
    }
  }
}
