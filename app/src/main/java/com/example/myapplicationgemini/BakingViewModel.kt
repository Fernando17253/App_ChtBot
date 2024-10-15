package com.example.myapplicationgemini

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class BakingViewModel(application: Application) : AndroidViewModel(application) {

  // Estado de la UI
  private val _uiState: MutableStateFlow<UiState> = MutableStateFlow(UiState.Initial)
  val uiState: StateFlow<UiState> = _uiState.asStateFlow()

  // Modelo generativo
  private val generativeModel = GenerativeModel(
    modelName = "gemini-1.5-flash",
    apiKey = BuildConfig.apiKey
  )

  // SharedPreferences para guardar el historial
  private val sharedPreferences = application.getSharedPreferences("baking_history", Context.MODE_PRIVATE)

  // Estructura de datos que contiene la pregunta y la respuesta
  data class QuestionAnswer(
    val question: String,
    val answer: String,
    val timestamp: LocalDateTime = LocalDateTime.now()  // Añadir marca de tiempo
  )

  // Historial de preguntas y respuestas
  private var _history = mutableListOf<QuestionAnswer>()
  val history: List<QuestionAnswer> get() = _history

  init {
    // Cargar historial desde SharedPreferences al iniciar
    loadHistory()
  }

  // Función para enviar el prompt al modelo generativo
  fun sendPrompt(prompt: String) {
    _uiState.value = UiState.Loading

    viewModelScope.launch(Dispatchers.IO) {
      try {
        // Llamada al modelo generativo
        val response = generativeModel.generateContent(
          content {
            text(prompt)
          }
        )

        // Procesar la respuesta del modelo
        response.text?.let { outputContent ->
          _uiState.value = UiState.Success(outputContent)

          // Agregar la respuesta al historial junto con el timestamp actual
          addToHistory(prompt, outputContent, LocalDateTime.now())
        }
      } catch (e: Exception) {
        _uiState.value = UiState.Error(e.localizedMessage ?: "")
      }
    }
  }

  // Función para construir el contexto con las últimas cuatro preguntas y respuestas
  private fun buildContextWithLastFourConversations(): String {
    val lastConversations = _history.takeLast(4)  // Obtener las últimas 4 interacciones
    val contextBuilder = StringBuilder()

    lastConversations.forEach { qa ->
      contextBuilder.append("Pregunta: ${qa.question}\n")
      contextBuilder.append("Respuesta: ${qa.answer}\n\n")
    }

    return contextBuilder.toString()
  }

  // Función para agregar una pregunta y su respuesta al historial
  private fun addToHistory(question: String, answer: String, timestamp: LocalDateTime) {
    _history.add(QuestionAnswer(question, answer, timestamp))
    saveHistory()
  }

  // Guardar historial en SharedPreferences
  private fun saveHistory() {
    viewModelScope.launch(Dispatchers.IO) {
      val jsonArray = JSONArray()
      _history.forEach { item ->
        // Convertimos cada entrada en un JSONObject antes de añadirlo al JSONArray
        val jsonObject = JSONObject().apply {
          put("question", item.question)
          put("answer", item.answer)
        }
        jsonArray.put(jsonObject)
      }
      sharedPreferences.edit().putString("history_key", jsonArray.toString()).apply()
    }
  }

  // Cargar historial desde SharedPreferences
  private fun loadHistory() {
    viewModelScope.launch(Dispatchers.IO) {
      val historyString = sharedPreferences.getString("history_key", null)
      if (!historyString.isNullOrEmpty()) {
        val jsonArray = JSONArray(historyString)
        for (i in 0 until jsonArray.length()) {
          val jsonObject = jsonArray.getJSONObject(i)
          val question = jsonObject.getString("question")
          val answer = jsonObject.getString("answer")
          _history.add(QuestionAnswer(question, answer))  // Reconstruimos la lista de historial
        }
        // Emitimos el estado actualizado después de cargar el historial
        _uiState.value = UiState.Success("Historial cargado")
      }
    }
  }

  // Función para borrar
  fun clearHistory() {
    // Limpiar el historial en memoria
    _history.clear()

    // Limpiar el historial en SharedPreferences
    sharedPreferences.edit().remove("history_key").apply()

    // Emitimos un nuevo estado de éxito con un historial vacío
    _uiState.value = UiState.Success("Historial limpiado")
  }
}
