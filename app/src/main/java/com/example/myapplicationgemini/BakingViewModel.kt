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


import android.speech.tts.TextToSpeech
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale

class BakingViewModel(application: Application) : AndroidViewModel(application), TextToSpeech.OnInitListener {

  private val _uiState: MutableStateFlow<UiState> = MutableStateFlow(UiState.Initial)
  val uiState: StateFlow<UiState> = _uiState.asStateFlow()

  private val generativeModel = GenerativeModel(
    modelName = "gemini-1.5-flash",
    apiKey = BuildConfig.apiKey
  )

  private val sharedPreferences = application.getSharedPreferences("baking_history", Context.MODE_PRIVATE)

  data class QuestionAnswer(
    val question: String,
    val answer: String,
    val timestamp: LocalDateTime = LocalDateTime.now()
  )

  private var _history = mutableListOf<QuestionAnswer>()
  val history: List<QuestionAnswer> get() = _history

  // Text-to-Speech (TTS)
  private var tts: TextToSpeech = TextToSpeech(application.applicationContext, this)
  private var isPaused: Boolean = false

  init {
    loadHistory()
  }

  // Enviar el prompt
  fun sendPrompt(prompt: String) {
    _uiState.value = UiState.Loading

    viewModelScope.launch(Dispatchers.IO) {
      try {
        val response = generativeModel.generateContent(
          content {
            text(prompt)
          }
        )

        response.text?.let { outputContent ->
          _uiState.value = UiState.Success(outputContent)
          addToHistory(prompt, outputContent, LocalDateTime.now())
        }
      } catch (e: Exception) {
        _uiState.value = UiState.Error(e.localizedMessage ?: "")
      }
    }
  }

  // Función para reproducir texto como audio
  fun speakText(text: String) {
    if (isPaused) {
      tts.playSilentUtterance(1L, TextToSpeech.QUEUE_ADD, null)  // Continuar reproducción
    } else {
      tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "")
    }
    isPaused = false
  }

  // Función para detener completamente el TTS
  fun stopText() {
    tts.stop()
    isPaused = false
  }

  // Inicializar el Text-to-Speech
  override fun onInit(status: Int) {
    if (status == TextToSpeech.SUCCESS) {
      tts.language = Locale.getDefault()  // Puedes cambiar el idioma si lo prefieres
    }
  }

  // Función para detener el TTS cuando se termine
  override fun onCleared() {
    tts.stop()
    tts.shutdown()
    super.onCleared()
  }

  private fun addToHistory(question: String, answer: String, timestamp: LocalDateTime) {
    _history.add(QuestionAnswer(question, answer, timestamp))
    saveHistory()
  }

  private fun saveHistory() {
    viewModelScope.launch(Dispatchers.IO) {
      val jsonArray = JSONArray()
      _history.forEach { item ->
        val jsonObject = JSONObject().apply {
          put("question", item.question)
          put("answer", item.answer)
        }
        jsonArray.put(jsonObject)
      }
      sharedPreferences.edit().putString("history_key", jsonArray.toString()).apply()
    }
  }

  private fun loadHistory() {
    viewModelScope.launch(Dispatchers.IO) {
      val historyString = sharedPreferences.getString("history_key", null)
      if (!historyString.isNullOrEmpty()) {
        val jsonArray = JSONArray(historyString)
        for (i in 4 until jsonArray.length()) {
          val jsonObject = jsonArray.getJSONObject(i)
          val question = jsonObject.getString("question")
          val answer = jsonObject.getString("answer")
          _history.add(QuestionAnswer(question, answer))
        }
        _uiState.value = UiState.Success("Historial cargado")
      }
    }
  }

  fun clearHistory() {
    _history.clear()
    sharedPreferences.edit().remove("history_key").apply()
    _uiState.value = UiState.Success("Historial limpiado")
  }
}
