package com.example.ui.game

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.MagentaNeon
import com.example.ui.theme.PurpleAccent
import kotlinx.coroutines.delay
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameStudioScreen(viewModel: GameViewModel = viewModel()) {
    var activeGameTab by remember { mutableStateOf(0) } // 0: AI Quiz, 1: Text RPG, 2: AI Arcade

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.SportsEsports,
                            contentDescription = null,
                            tint = MagentaNeon,
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("AI Game Creator Studio", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Mode Tabs
            SecondaryScrollableTabRow(
                selectedTabIndex = activeGameTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MagentaNeon
            ) {
                Tab(
                    selected = activeGameTab == 0,
                    onClick = { activeGameTab = 0 },
                    text = { Text("AI Quiz Generator", fontSize = 13.sp) },
                    icon = { Icon(Icons.Default.Psychology, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = activeGameTab == 1,
                    onClick = { activeGameTab = 1 },
                    text = { Text("RPG Petualangan AI", fontSize = 13.sp) },
                    icon = { Icon(Icons.Default.Explore, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = activeGameTab == 2,
                    onClick = { activeGameTab = 2 },
                    text = { Text("Arcade Dodge Studio", fontSize = 13.sp) },
                    icon = { Icon(Icons.Default.Gamepad, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            when (activeGameTab) {
                0 -> QuizGameView(viewModel)
                1 -> TextRpgGameView(viewModel)
                2 -> ArcadeGameView(viewModel)
            }
        }
    }
}

// --- 1. AI Quiz Game View ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizGameView(viewModel: GameViewModel) {
    val topicInput by viewModel.quizTopicInput.collectAsState()
    val quizConfig by viewModel.currentQuiz.collectAsState()
    val isLoading by viewModel.isQuizLoading.collectAsState()
    val qIndex by viewModel.currentQuestionIndex.collectAsState()
    val score by viewModel.quizScore.collectAsState()
    val selectedOption by viewModel.selectedOptionIndex.collectAsState()
    val isSubmitted by viewModel.isAnswerSubmitted.collectAsState()

    var customTopicText by remember { mutableStateOf(topicInput) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Generator Controls
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("Buat Kuis AI Baru dari Topik Pilihanmu:", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = customTopicText,
                        onValueChange = { customTopicText = it },
                        placeholder = { Text("Contoh: Pemrograman Kotlin, Sejarah World, Pop Culture...") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("quiz_topic_input"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { viewModel.generateQuiz(customTopicText) },
                        enabled = !isLoading && customTopicText.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary, contentColor = Color.Black),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("generate_quiz_button")
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.Black, strokeWidth = 2.dp)
                        } else {
                            Text("Buat Game")
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = CyanPrimary)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("AI sedang menyusun pertanyaan kuis interaktif...", style = MaterialTheme.typography.bodyMedium)
                }
            }
        } else if (quizConfig != null) {
            val questions = quizConfig!!.questions
            val currentQ = questions.getOrNull(qIndex)

            if (currentQ != null) {
                // Progress & Score Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Soal ${qIndex + 1} dari ${questions.size}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = CyanPrimary
                    )
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = AmberGold.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "Skor: $score PK",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            fontWeight = FontWeight.Bold,
                            color = AmberGold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Question Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("quiz_question_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = currentQ.question,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Options
                        currentQ.options.forEachIndexed { optIdx, optText ->
                            val isSelected = selectedOption == optIdx
                            val isCorrect = optIdx == currentQ.correctAnswerIndex

                            val cardColor = when {
                                isSubmitted && isCorrect -> EmeraldGreen.copy(alpha = 0.25f)
                                isSubmitted && isSelected && !isCorrect -> MaterialTheme.colorScheme.error.copy(alpha = 0.25f)
                                isSelected -> CyanPrimary.copy(alpha = 0.2f)
                                else -> MaterialTheme.colorScheme.surface
                            }

                            val borderColor = when {
                                isSubmitted && isCorrect -> EmeraldGreen
                                isSubmitted && isSelected && !isCorrect -> MaterialTheme.colorScheme.error
                                isSelected -> CyanPrimary
                                else -> MaterialTheme.colorScheme.outline
                            }

                            OutlinedCard(
                                onClick = { viewModel.submitQuizAnswer(optIdx) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, borderColor),
                                colors = CardDefaults.cardColors(containerColor = cardColor)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${('A' + optIdx)}. ",
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) CyanPrimary else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = optText,
                                        modifier = Modifier.weight(1f),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    if (isSubmitted) {
                                        if (isCorrect) {
                                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldGreen)
                                        } else if (isSelected) {
                                            Icon(Icons.Default.Cancel, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                }
                            }
                        }

                        // Explanation Box if submitted
                        if (isSubmitted) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surface
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("💡 Penjelasan AI:", fontWeight = FontWeight.Bold, color = CyanPrimary, fontSize = 13.sp)
                                    Text(currentQ.explanation, style = MaterialTheme.typography.bodySmall)
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            if (qIndex < questions.size - 1) {
                                Button(
                                    onClick = { viewModel.nextQuestion() },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("next_quiz_question_button"),
                                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary, contentColor = Color.Black)
                                ) {
                                    Text("Pertanyaan Berikutnya", fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(Icons.Default.ArrowForward, contentDescription = null)
                                }
                            } else {
                                Button(
                                    onClick = { viewModel.generateQuiz(customTopicText) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen, contentColor = Color.White)
                                ) {
                                    Text("Kuis Selesai! Main Lagi", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- 2. AI Text RPG Game View ---
@Composable
fun TextRpgGameView(viewModel: GameViewModel) {
    val rpgScene by viewModel.currentRpgScene.collectAsState()
    val isLoading by viewModel.isRpgLoading.collectAsState()
    val playerHp by viewModel.playerHp.collectAsState()
    val inventory by viewModel.playerInventory.collectAsState()

    var newConceptText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Player Status Dashboard
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Favorite, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("HP: $playerHp / 100", fontWeight = FontWeight.Bold)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Place, contentDescription = null, tint = CyanPrimary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(rpgScene?.locationName ?: "Aura Cyber City", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "🎒 Barang Ditemukan: ${inventory.joinToString(", ")}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Main RPG Story Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("rpg_story_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AutoStories, contentDescription = null, tint = PurpleAccent)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Alur Petualangan RPG", fontWeight = FontWeight.Bold, color = PurpleAccent)
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(color = PurpleAccent, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Game Master AI sedang merancang kelanjutan alur cerita...")
                        }
                    }
                } else if (rpgScene != null) {
                    Text(
                        text = rpgScene!!.storyText,
                        style = MaterialTheme.typography.bodyLarge,
                        lineHeight = 22.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    if (rpgScene!!.isGameOver) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.errorContainer
                        ) {
                            Text(
                                text = "💀 GAME OVER! Silakan mulai petualangan baru di bawah.",
                                modifier = Modifier.padding(12.dp),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    } else {
                        Text(
                            text = "Pilih Langkah Kamu Selanjutnya:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = CyanPrimary
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        rpgScene!!.choices.forEach { choice ->
                            OutlinedButton(
                                onClick = { viewModel.chooseRpgOption(choice) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, CyanPrimary)
                            ) {
                                Text(choice.text, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Start)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Reset RPG Story Input
        OutlinedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("Mulai Misi RPG Baru:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Row {
                    OutlinedTextField(
                        value = newConceptText,
                        onValueChange = { newConceptText = it },
                        placeholder = { Text("Contoh: Cari harta karun di planet luar angkasa...") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (newConceptText.isNotBlank()) {
                                viewModel.startNewRpgGame(newConceptText)
                                newConceptText = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PurpleAccent)
                    ) {
                        Text("Mulai")
                    }
                }
            }
        }
    }
}

// --- 3. AI Arcade Dodge Mini-Game View ---
@Composable
fun ArcadeGameView(viewModel: GameViewModel) {
    var playerX by remember { mutableStateOf(200f) }
    var score by remember { mutableStateOf(0) }
    var isGameOver by remember { mutableStateOf(false) }

    // Obstacle state
    var obstacleY by remember { mutableStateOf(-50f) }
    var obstacleX by remember { mutableStateOf(150f) }

    // Star item state
    var starY by remember { mutableStateOf(-100f) }
    var starX by remember { mutableStateOf(250f) }

    // Game Loop Timer
    LaunchedEffect(isGameOver) {
        if (!isGameOver) {
            while (true) {
                delay(30)
                // Move obstacle down
                obstacleY += 12f
                if (obstacleY > 700f) {
                    obstacleY = -50f
                    obstacleX = Random.nextInt(50, 350).toFloat()
                    score += 1
                }

                // Move star down
                starY += 10f
                if (starY > 700f) {
                    starY = -100f
                    starX = Random.nextInt(50, 350).toFloat()
                }

                // Collision detection with player at bottom (Y=550)
                if (obstacleY in 520f..580f && kotlin.math.abs(obstacleX - playerX) < 45f) {
                    isGameOver = true
                }

                if (starY in 520f..580f && kotlin.math.abs(starX - playerX) < 45f) {
                    score += 5
                    starY = -100f
                    starX = Random.nextInt(50, 350).toFloat()
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Arcade Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("🕹️ Arcade AI: Space Dodge", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text("Skor: $score", fontWeight = FontWeight.Bold, color = CyanPrimary, fontSize = 16.sp)
        }

        Text(
            text = "Geser jari ke kiri / kanan untuk mengendalikan roket & hindari meteor!",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Interactive Canvas Game Area
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(420.dp)
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        playerX = (playerX + dragAmount.x).coerceIn(40f, size.width.toFloat() - 40f)
                    }
                }
                .testTag("arcade_game_canvas"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0B0F19)),
            border = BorderStroke(2.dp, CyanPrimary)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    // Draw Player Ship at bottom
                    drawCircle(
                        color = Color(0xFF00E5FF),
                        radius = 25f,
                        center = Offset(playerX, 550f)
                    )

                    // Draw Obstacle (Meteor)
                    drawCircle(
                        color = Color(0xFFFF2A85),
                        radius = 22f,
                        center = Offset(obstacleX, obstacleY)
                    )

                    // Draw Star Bonus
                    drawCircle(
                        color = Color(0xFFF59E0B),
                        radius = 16f,
                        center = Offset(starX, starY)
                    )
                }

                if (isGameOver) {
                    Surface(
                        modifier = Modifier.align(Alignment.Center),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("💥 BOOM! GAME OVER", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                            Text("Skor Akhir: $score", fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    score = 0
                                    obstacleY = -50f
                                    starY = -100f
                                    isGameOver = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary, contentColor = Color.Black)
                            ) {
                                Text("Main Lagi")
                            }
                        }
                    }
                }
            }
        }
    }
}
