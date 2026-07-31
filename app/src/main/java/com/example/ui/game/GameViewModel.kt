package com.example.ui.game

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.*
import com.example.data.repository.GeminiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val dao = db.historyDao()
    private val repository = GeminiRepository(application)

    val savedGames = dao.getAllSavedGames()

    // --- Quiz Mode State ---
    val quizTopicInput = MutableStateFlow("Pengetahuan Umum & AI")
    val currentQuiz = MutableStateFlow<QuizGameConfig?>(null)
    val isQuizLoading = MutableStateFlow(false)
    val currentQuestionIndex = MutableStateFlow(0)
    val quizScore = MutableStateFlow(0)
    val selectedOptionIndex = MutableStateFlow<Int?>(null)
    val isAnswerSubmitted = MutableStateFlow(false)

    // --- Text RPG Mode State ---
    val rpgStoryHistory = MutableStateFlow(StringBuilder("Petualangan dimulai di Kota Cyber Aura..."))
    val currentRpgScene = MutableStateFlow<RpgScene?>(null)
    val isRpgLoading = MutableStateFlow(false)
    val playerHp = MutableStateFlow(100)
    val playerInventory = MutableStateFlow(mutableListOf("Kartu Akses AI"))

    // --- Arcade Mini-Game State ---
    val arcadeTitleInput = MutableStateFlow("Space Dodge AI")
    val currentArcadeConfig = MutableStateFlow(ArcadeGameConfig("Space Dodge AI", "#00E5FF", 1.2f, 1.5f, 15, "Bintang", "Meteor"))

    init {
        // Start initial default Quiz and RPG Scene
        generateQuiz("Sains & AI")
        startNewRpgGame("Jelajahi Lab Rahasia AI")
    }

    // --- Quiz Methods ---
    fun generateQuiz(topic: String) {
        val topicName = topic.ifBlank { "Teknologi AI" }
        isQuizLoading.value = true
        currentQuestionIndex.value = 0
        quizScore.value = 0
        selectedOptionIndex.value = null
        isAnswerSubmitted.value = false

        viewModelScope.launch {
            val config = repository.generateQuizGame(topicName)
            currentQuiz.value = config
            isQuizLoading.value = false

            // Save to DB
            dao.insertGame(
                SavedGame(
                    title = config.title,
                    gameType = GameType.QUIZ,
                    description = "Kuis 5 Soal tentang ${config.topic}",
                    jsonContent = topicName
                )
            )
        }
    }

    fun submitQuizAnswer(optionIdx: Int) {
        if (isAnswerSubmitted.value) return
        selectedOptionIndex.value = optionIdx
        isAnswerSubmitted.value = true

        val q = currentQuiz.value?.questions?.getOrNull(currentQuestionIndex.value) ?: return
        if (optionIdx == q.correctAnswerIndex) {
            quizScore.value += 20
        }
    }

    fun nextQuestion() {
        val quiz = currentQuiz.value ?: return
        if (currentQuestionIndex.value < quiz.questions.size - 1) {
            currentQuestionIndex.value += 1
            selectedOptionIndex.value = null
            isAnswerSubmitted.value = false
        }
    }

    // --- Text RPG Methods ---
    fun startNewRpgGame(initialConcept: String) {
        isRpgLoading.value = true
        playerHp.value = 100
        playerInventory.value = mutableListOf("Kartu Akses AI")
        rpgStoryHistory.value = StringBuilder("Misi: $initialConcept.\n")

        viewModelScope.launch {
            val scene = repository.generateRpgScene(
                actionPrompt = initialConcept,
                storyHistory = rpgStoryHistory.value.toString(),
                currentHealth = playerHp.value
            )
            currentRpgScene.value = scene
            isRpgLoading.value = false
        }
    }

    fun chooseRpgOption(choice: RpgChoice) {
        val scene = currentRpgScene.value ?: return
        isRpgLoading.value = true

        // Append story
        rpgStoryHistory.value.append("\n\n[Pilihan Pemain]: ${choice.text}\n${scene.storyText}")

        // Apply health change
        val newHp = (playerHp.value + scene.healthChange).coerceIn(0, 100)
        playerHp.value = newHp

        // Apply item
        scene.itemGained?.let {
            if (it.isNotBlank() && !playerInventory.value.contains(it)) {
                playerInventory.value.add(it)
            }
        }

        if (newHp <= 0) {
            currentRpgScene.value = RpgScene(
                storyText = "Kesehatan kamu habis! Game Over.",
                locationName = scene.locationName,
                isGameOver = true,
                choices = emptyList()
            )
            isRpgLoading.value = false
            return
        }

        viewModelScope.launch {
            val nextScene = repository.generateRpgScene(
                actionPrompt = choice.actionPrompt,
                storyHistory = rpgStoryHistory.value.toString(),
                currentHealth = playerHp.value
            )
            currentRpgScene.value = nextScene
            isRpgLoading.value = false
        }
    }

    // --- Arcade Methods ---
    fun updateArcadeConfig(title: String, speed: Float) {
        currentArcadeConfig.value = ArcadeGameConfig(
            title = title,
            playerSpeed = speed,
            obstacleSpeed = speed * 1.2f,
            targetScore = 15,
            itemName = "Item Neon",
            obstacleName = "Bahaya"
        )
    }
}
