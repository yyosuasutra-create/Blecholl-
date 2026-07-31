package com.example.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import com.example.BuildConfig
import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

class GeminiRepository(private val context: Context) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    // SharedPreferences for dynamic key backup
    private val prefs = context.getSharedPreferences("aura_ai_prefs", Context.MODE_PRIVATE)

    fun getApiKey(): String {
        val userKey = prefs.getString("custom_api_key", "") ?: ""
        if (userKey.isNotBlank()) return userKey
        return try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }
    }

    fun saveApiKey(key: String) {
        prefs.edit().putString("custom_api_key", key.trim()).apply()
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        // Compress bitmap to reasonable size for Gemini API (max 1024x1024)
        val scaled = if (bitmap.width > 1024 || bitmap.height > 1024) {
            val ratio = Math.min(1024.0 / bitmap.width, 1024.0 / bitmap.height)
            Bitmap.createScaledBitmap(
                bitmap,
                (bitmap.width * ratio).toInt(),
                (bitmap.height * ratio).toInt(),
                true
            )
        } else {
            bitmap
        }
        scaled.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }

    // --- Core Gemini Text & Multimodal API Call ---
    suspend fun generateContent(
        prompt: String,
        bitmap: Bitmap? = null,
        systemInstruction: String? = null
    ): String = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isBlank()) {
            return@withContext "⚠️ API Key Gemini belum dikonfigurasi. Silakan masukkan API Key di menu Pengaturan MVP atau gunakan mode offline/preset."
        }

        // Gemini 2.5 Flash / 1.5 Flash Endpoint
        val primaryModel = "gemini-2.5-flash"
        val fallbackModel = "gemini-1.5-flash"

        try {
            return@withContext executeGeminiRequest(primaryModel, apiKey, prompt, bitmap, systemInstruction)
        } catch (e: Exception) {
            Log.e("GeminiRepository", "Primary model failed, trying fallback: ${e.message}")
            try {
                return@withContext executeGeminiRequest(fallbackModel, apiKey, prompt, bitmap, systemInstruction)
            } catch (e2: Exception) {
                Log.e("GeminiRepository", "Fallback model failed: ${e2.message}")
                return@withContext "Gagal menghubungi AI (${e2.localizedMessage}). Pastikan koneksi internet stabil dan API Key valid."
            }
        }
    }

    private fun executeGeminiRequest(
        model: String,
        apiKey: String,
        prompt: String,
        bitmap: Bitmap?,
        systemInstruction: String?
    ): String {
        val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"

        val rootJson = JSONObject()
        val contentsArray = JSONArray()
        val contentObj = JSONObject()
        val partsArray = JSONArray()

        // Image part if available
        if (bitmap != null) {
            val base64Data = bitmapToBase64(bitmap)
            val inlineDataObj = JSONObject().apply {
                put("mime_type", "image/jpeg")
                put("data", base64Data)
            }
            val imagePartObj = JSONObject().apply {
                put("inline_data", inlineDataObj)
            }
            partsArray.put(imagePartObj)
        }

        // Text prompt part
        val textPartObj = JSONObject().apply {
            put("text", prompt)
        }
        partsArray.put(textPartObj)

        contentObj.put("parts", partsArray)
        contentsArray.put(contentObj)
        rootJson.put("contents", contentsArray)

        // System Instruction if provided
        if (!systemInstruction.isNullOrBlank()) {
            val sysInstructionObj = JSONObject().apply {
                val sysParts = JSONArray().put(JSONObject().put("text", systemInstruction))
                put("parts", sysParts)
            }
            rootJson.put("system_instruction", sysInstructionObj)
        }

        // Generation Config
        val genConfig = JSONObject().apply {
            put("temperature", 0.7)
            put("maxOutputTokens", 2048)
        }
        rootJson.put("generationConfig", genConfig)

        val body = rootJson.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        client.newCall(request).execute().use { response ->
            val responseBodyString = response.body?.string() ?: ""
            if (!response.isSuccessful) {
                Log.e("GeminiRepository", "API Error HTTP ${response.code}: $responseBodyString")
                throw Exception("HTTP ${response.code}: $responseBodyString")
            }

            val resJson = JSONObject(responseBodyString)
            val candidates = resJson.optJSONArray("candidates")
            if (candidates != null && candidates.length() > 0) {
                val firstCandidate = candidates.getJSONObject(0)
                val content = firstCandidate.optJSONObject("content")
                val parts = content?.optJSONArray("parts")
                if (parts != null && parts.length() > 0) {
                    val sb = StringBuilder()
                    for (i in 0 until parts.length()) {
                        sb.append(parts.getJSONObject(i).optString("text", ""))
                    }
                    return sb.toString()
                }
            }
            return "Respons kosong dari AI."
        }
    }

    // --- Specialized Method: Generate AI Quiz Game JSON ---
    suspend fun generateQuizGame(topic: String): QuizGameConfig = withContext(Dispatchers.IO) {
        val prompt = """
            Buatlah kuis interaktif 5 pertanyaan dengan topik: "$topic".
            Berikan output HARUS dalam format JSON murni berikut tanpa pemformatan markdown lain:
            {
              "title": "Kuis: $topic",
              "topic": "$topic",
              "questions": [
                {
                  "id": 1,
                  "question": "Pertanyaan 1 di sini?",
                  "options": ["Pilihan A", "Pilihan B", "Pilihan C", "Pilihan D"],
                  "correctAnswerIndex": 0,
                  "explanation": "Penjelasan mengapa jawaban tersebut benar."
                }
              ]
            }
        """.trimIndent()

        val sysInstruction = "Kamu adalah Game Master Kuis AI yang membuat kuis mendidik, seru, dan akurat dalam bahasa Indonesia."
        val rawResponse = generateContent(prompt, systemInstruction = sysInstruction)

        return@withContext parseQuizConfig(topic, rawResponse)
    }

    private fun parseQuizConfig(topic: String, rawResponse: String): QuizGameConfig {
        try {
            // Extract json string from response
            var jsonStr = rawResponse.trim()
            if (jsonStr.contains("```json")) {
                jsonStr = jsonStr.substringAfter("```json").substringBefore("```").trim()
            } else if (jsonStr.contains("```")) {
                jsonStr = jsonStr.substringAfter("```").substringBefore("```").trim()
            }

            val obj = JSONObject(jsonStr)
            val title = obj.optString("title", "Kuis $topic")
            val questionsArray = obj.getJSONArray("questions")
            val questionsList = mutableListOf<QuizQuestion>()

            for (i in 0 until questionsArray.length()) {
                val qObj = questionsArray.getJSONObject(i)
                val optionsArray = qObj.getJSONArray("options")
                val options = mutableListOf<String>()
                for (j in 0 until optionsArray.length()) {
                    options.add(optionsArray.getString(j))
                }

                questionsList.add(
                    QuizQuestion(
                        id = qObj.optInt("id", i + 1),
                        question = qObj.optString("question", "Pertanyaan ${i + 1}"),
                        options = options,
                        correctAnswerIndex = qObj.optInt("correctAnswerIndex", 0),
                        explanation = qObj.optString("explanation", "Penjelasan kuis.")
                    )
                )
            }

            if (questionsList.isNotEmpty()) {
                return QuizGameConfig(title, topic, questionsList)
            }
        } catch (e: Exception) {
            Log.e("GeminiRepository", "Failed to parse quiz json: ${e.message}")
        }

        // Fallback default quiz if parsing fails or key is missing
        return QuizGameConfig(
            title = "Kuis AI MVP: $topic",
            topic = topic,
            questions = listOf(
                QuizQuestion(
                    id = 1,
                    question = "Apa fungsi utama dari AI Studio Aura?",
                    options = listOf("Editing Foto, Chat, & Buat Game", "Hanya Kirim Pesan", "Hanya Main Game", "Pemutar Musik"),
                    correctAnswerIndex = 0,
                    explanation = "Aura AI Studio adalah aplikasi all-in-one untuk foto, chat AI, dan game generator!"
                ),
                QuizQuestion(
                    id = 2,
                    question = "Model AI mana yang digunakan untuk multimodal cepat?",
                    options = listOf("Gemini 2.5 / 1.5 Flash", "Legacy GPT-1", "Simple Rules Engine", "Static Text"),
                    correctAnswerIndex = 0,
                    explanation = "Gemini Flash dirancang untuk multimodal cepat dan efisien."
                ),
                QuizQuestion(
                    id = 3,
                    question = "Bagaimana game AI dapat dibuat?",
                    options = listOf("Dengan prompt teks sederhana", "Mengodekan C++ 10.000 baris", "Membeli kaset", "Harus sewa server khusus"),
                    correctAnswerIndex = 0,
                    explanation = "Cukup berikan topik prompt, AI membuat struktur kuis dan RPG secara otomatis!"
                )
            )
        )
    }

    // --- Specialized Method: Generate Text RPG Scene ---
    suspend fun generateRpgScene(
        actionPrompt: String,
        storyHistory: String,
        currentHealth: Int
    ): RpgScene = withContext(Dispatchers.IO) {
        val prompt = """
            Riwayat Cerita RPG Sejauh ini:
            $storyHistory

            Aksi Terakhir Pemain: "$actionPrompt"
            Sisa Health Pemain: $currentHealth

            Lanjutkan petualangan sci-fi / fantasi cyber ini!
            Berikan output HARUS dalam format JSON murni berikut:
            {
              "storyText": "Teks kelanjutan cerita yang mendebarkan (2-4 kalimat)...",
              "locationName": "Nama Lokasi / Ruangan Baru",
              "healthChange": 0, (misal -10 jika kena jebakan, +15 jika dapat obat/istirahat, 0 jika normal)
              "itemGained": "Pedang Laser" (atau null jika tidak ada),
              "isGameOver": false,
              "isVictory": false,
              "choices": [
                {
                  "id": "choice_1",
                  "text": "Deskripsi pilihan 1 yang akan diambil pemain",
                  "actionPrompt": "Aksi detail pilihan 1"
                },
                {
                  "id": "choice_2",
                  "text": "Deskripsi pilihan 2 yang akan diambil pemain",
                  "actionPrompt": "Aksi detail pilihan 2"
                }
              ]
            }
        """.trimIndent()

        val sysInstruction = "Kamu adalah Game Master RPG AI yang imersif dan adil dalam bahasa Indonesia."
        val rawResponse = generateContent(prompt, systemInstruction = sysInstruction)

        return@withContext parseRpgScene(rawResponse)
    }

    private fun parseRpgScene(rawResponse: String): RpgScene {
        try {
            var jsonStr = rawResponse.trim()
            if (jsonStr.contains("```json")) {
                jsonStr = jsonStr.substringAfter("```json").substringBefore("```").trim()
            } else if (jsonStr.contains("```")) {
                jsonStr = jsonStr.substringAfter("```").substringBefore("```").trim()
            }

            val obj = JSONObject(jsonStr)
            val choicesArray = obj.optJSONArray("choices")
            val choicesList = mutableListOf<RpgChoice>()
            if (choicesArray != null) {
                for (i in 0 until choicesArray.length()) {
                    val cObj = choicesArray.getJSONObject(i)
                    choicesList.add(
                        RpgChoice(
                            id = cObj.optString("id", "choice_$i"),
                            text = cObj.optString("text", "Pilihan $i"),
                            actionPrompt = cObj.optString("actionPrompt", "Aksi $i")
                        )
                    )
                }
            }

            return RpgScene(
                storyText = obj.optString("storyText", "Kamu melanjutkan perjalanan di dalam dunia AI..."),
                locationName = obj.optString("locationName", "Sektor Cyber 01"),
                healthChange = obj.optInt("healthChange", 0),
                itemGained = if (obj.isNull("itemGained")) null else obj.optString("itemGained"),
                choices = choicesList,
                isGameOver = obj.optBoolean("isGameOver", false),
                isVictory = obj.optBoolean("isVictory", false)
            )
        } catch (e: Exception) {
            Log.e("GeminiRepository", "Failed to parse RPG scene: ${e.message}")
        }

        // Fallback RPG Scene
        return RpgScene(
            storyText = "Kamu tiba di gerbang benteng neon yang dipenuhi sinyal misterius. Lampu indikator berkedip lembut saat kamu mendekat.",
            locationName = "Gerbang Benteng Cyber",
            healthChange = 0,
            itemGained = "Kartu Akses Aura",
            choices = listOf(
                RpgChoice("c1", "Retas panel pintu masuk dengan Kartu Aura", "Pergi ke panel kontrol pintu dan lakukan peretasan cepat"),
                RpgChoice("c2", "Cari ventilasi rahasia di samping benteng", "Mengelilingi dinding barat untuk mencari celah masuk")
            )
        )
    }
}
