package com.example.ui.mvp

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.*
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.GeminiRepository
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.MagentaNeon
import com.example.ui.theme.PurpleAccent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MvpDashboardScreen(
    onNavigateToPhoto: () -> Unit,
    onNavigateToChat: () -> Unit,
    onNavigateToGame: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember { GeminiRepository(context) }

    var apiKeyText by remember { mutableStateOf(repository.getApiKey()) }
    var showApiKeyDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.RocketLaunch,
                            contentDescription = null,
                            tint = CyanPrimary,
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Aura AI MVP Hub", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showApiKeyDialog = true },
                        modifier = Modifier.testTag("api_key_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Key,
                            contentDescription = "Pengaturan API Key",
                            tint = AmberGold
                        )
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // MVP Status Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("mvp_status_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🚀 MVP Ready Version 1.0",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = EmeraldGreen.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "Aktif",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                fontWeight = FontWeight.Bold,
                                color = EmeraldGreen,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Aplikasi ini memenuhi seluruh kebutuhan MVP (Minimum Viable Product): Edit foto dengan AI, Tanya jawab chatbot, dan Studio generator game.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // API Key Status Badge
                    val currentKey = repository.getApiKey()
                    val isKeySet = currentKey.isNotBlank()

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isKeySet) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (isKeySet) EmeraldGreen else AmberGold,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isKeySet) "API Key Gemini: Siap Digunakan" else "API Key Gemini: Belum Diatur (Gunakan Tombol Kunci)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isKeySet) EmeraldGreen else AmberGold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Fitur Utama Aplikasi MVP:",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Quick Launcher 1: Photo Editor
            MvpFeatureCard(
                title = "📸 AI Photo Editor & Filters",
                description = "Edit foto dengan filter real-time, penyesuaian kecerahan/kontras, serta analisis komposisi & ide gaya dari Gemini AI.",
                badgeText = "Editor Foto",
                badgeColor = CyanPrimary,
                onClick = onNavigateToPhoto
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Quick Launcher 2: Chat AI
            MvpFeatureCard(
                title = "💬 Tanya Jawab Chatbot AI",
                description = "Percakapan cerdas dengan pilihan persona AI (Asisten, Pakar Tekno, Game Master, Guru). Mendukung tanya jawab teks & gambar.",
                badgeText = "Tanya Jawab",
                badgeColor = PurpleAccent,
                onClick = onNavigateToChat
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Quick Launcher 3: Game Studio
            MvpFeatureCard(
                title = "🎮 Pembuat Game AI & Studio",
                description = "Generator kuis interaktif otomatis, petualangan RPG bercabang AI, serta game arcade Dodge yang playable!",
                badgeText = "Buat Game",
                badgeColor = MagentaNeon,
                onClick = onNavigateToGame
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Architecture Info Box
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(" Arsitektur & Teknologi MVP", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("• Kotlin Coroutines & Flow untuk penanganan asynchronous", style = MaterialTheme.typography.bodySmall)
                    Text("• Jetpack Compose & Material 3 untuk UI modern dan responsif", style = MaterialTheme.typography.bodySmall)
                    Text("• Room Database untuk penyimpanan riwayat chat & game lokal", style = MaterialTheme.typography.bodySmall)
                    Text("• Gemini REST Multimodal API untuk AI vision, text, dan JSON parsing", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }

    // API Key Dialog
    if (showApiKeyDialog) {
        AlertDialog(
            onDismissRequest = { showApiKeyDialog = false },
            title = { Text("Pengaturan API Key Gemini") },
            text = {
                Column {
                    Text(
                        "Masukkan API Key Gemini untuk mengaktifkan AI secara penuh. Kunci ini disimpan secara lokal dan aman di perangkat kamu.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = apiKeyText,
                        onValueChange = { apiKeyText = it },
                        label = { Text("API Key Gemini") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        repository.saveApiKey(apiKeyText)
                        showApiKeyDialog = false
                        Toast.makeText(context, "API Key berhasil disimpan!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary, contentColor = MaterialTheme.colorScheme.onPrimary)
                ) {
                    Text("Simpan Key")
                }
            },
            dismissButton = {
                TextButton(onClick = { showApiKeyDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
fun MvpFeatureCard(
    title: String,
    description: String,
    badgeText: String,
    badgeColor: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    OutlinedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = badgeColor.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = badgeText,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = badgeColor
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = badgeColor
            )
        }
    }
}
