package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

// --- Chat Data Models ---
enum class Sender { USER, AI }

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val text: String,
    val sender: Sender,
    val timestamp: Long = System.currentTimeMillis(),
    val imageUri: String? = null
)

// --- Game Generator Data Models ---
enum class GameType { TEXT_RPG, QUIZ, ARCADE }

data class QuizQuestion(
    val id: Int,
    val question: String,
    val options: List<String>,
    val correctAnswerIndex: Int,
    val explanation: String
)

data class QuizGameConfig(
    val title: String,
    val topic: String,
    val questions: List<QuizQuestion>
)

data class RpgChoice(
    val id: String,
    val text: String,
    val actionPrompt: String
)

data class RpgScene(
    val storyText: String,
    val locationName: String,
    val healthChange: Int = 0,
    val itemGained: String? = null,
    val choices: List<RpgChoice>,
    val isGameOver: Boolean = false,
    val isVictory: Boolean = false
)

data class ArcadeGameConfig(
    val title: String,
    val themeColorHex: String = "#00E5FF",
    val playerSpeed: Float = 1.0f,
    val obstacleSpeed: Float = 1.0f,
    val targetScore: Int = 10,
    val itemName: String = "Bintang",
    val obstacleName: String = "Rintangan"
)

@Entity(tableName = "saved_games")
data class SavedGame(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val gameType: GameType,
    val description: String,
    val jsonContent: String,
    val createdAt: Long = System.currentTimeMillis()
)

// --- Photo Edit Preset ---
data class PhotoFilterPreset(
    val name: String,
    val brightness: Float = 1.0f,
    val contrast: Float = 1.0f,
    val saturation: Float = 1.0f,
    val vintage: Boolean = false,
    val cyber: Boolean = false,
    val grayscale: Boolean = false,
    val description: String
)
