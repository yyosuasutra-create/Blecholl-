package com.example.ui.chat

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.ChatMessage
import com.example.data.model.Sender
import com.example.data.repository.GeminiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class AiPersona(val title: String, val systemInstruction: String) {
    ASSISTANT("Asisten Cerdas", "Kamu adalah Asisten AI serba bisa, ramah, dan solutif yang menjawab dalam bahasa Indonesia."),
    TECH_EXPERT("Pakar Tekno", "Kamu adalah Pakar Teknologi & Pemrograman berpengalaman. Berikan penjelasan teknis yang mendalam, kode bersih, dan arsitektur Android/Kotlin/AI."),
    GAME_MASTER("Game Master", "Kamu adalah Game Master profesional yang membantu merancang mekanisme game, alur cerita RPG, dan desain game interaktif."),
    CREATIVE_TEACHER("Guru Kreatif", "Kamu adalah Guru Kreatif yang dapat menjelaskan konsep sulit menjadi sangat sederhana dengan contoh yang menarik.")
}

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val dao = db.historyDao()
    private val repository = GeminiRepository(application)

    val messages = dao.getAllMessages()

    val selectedPersona = MutableStateFlow(AiPersona.ASSISTANT)
    val isLoading = MutableStateFlow(false)

    val selectedImageBitmap = MutableStateFlow<Bitmap?>(null)

    init {
        // Insert initial welcome message if empty
        viewModelScope.launch {
            dao.getAllMessages().collect { list ->
                if (list.isEmpty()) {
                    dao.insertMessage(
                        ChatMessage(
                            text = "Halo! Saya Aura AI. Saya siap membantu kamu menjawab pertanyaan, menulis kode, membuat ide game, atau membahas fotografi. Ada yang bisa saya bantu hari ini?",
                            sender = Sender.AI
                        )
                    )
                }
            }
        }
    }

    fun sendMessage(promptText: String) {
        if (promptText.isBlank() && selectedImageBitmap.value == null) return

        val textToSend = promptText.trim()
        val bmpToSend = selectedImageBitmap.value

        // Clear input attachment state
        selectedImageBitmap.value = null
        isLoading.value = true

        viewModelScope.launch {
            // Save user message to database
            dao.insertMessage(
                ChatMessage(
                    text = textToSend.ifBlank { "[Foto dilampirkan]" },
                    sender = Sender.USER
                )
            )

            // Call Gemini API
            val sysInstruction = selectedPersona.value.systemInstruction
            val aiReply = repository.generateContent(
                prompt = textToSend,
                bitmap = bmpToSend,
                systemInstruction = sysInstruction
            )

            // Save AI message
            dao.insertMessage(
                ChatMessage(
                    text = aiReply,
                    sender = Sender.AI
                )
            )

            isLoading.value = false
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            dao.clearChatHistory()
            dao.insertMessage(
                ChatMessage(
                    text = "Riwayat percakapan telah dibersihkan. Silakan ajukan pertanyaan baru!",
                    sender = Sender.AI
                )
            )
        }
    }
}
