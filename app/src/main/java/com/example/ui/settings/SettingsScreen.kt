package com.example.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.GeminiRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val repository = remember { GeminiRepository(context) }

    var apiKeyInput by remember { mutableStateOf(repository.getApiKey()) }
    var isKeySaved by remember { mutableStateOf(false) }
    var isPasswordVisible by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pengaturan MVP & AI Studio", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // API Key Configuration Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Key, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Konfigurasi API Key Gemini AI", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "API Key otomatis disuntikkan oleh AI Studio di runtime. Anda juga dapat memasukkan API Key kustom di sini jika ingin menggunakan akun Google AI milik Anda sendiri.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = apiKeyInput,
                        onValueChange = {
                            apiKeyInput = it
                            isKeySaved = false
                        },
                        label = { Text("Gemini API Key") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_api_key"),
                        singleLine = true,
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                Icon(
                                    if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Sembunyikan/Tampilkan Key"
                                )
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            repository.saveApiKey(apiKeyInput)
                            isKeySaved = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_save_api_key")
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isKeySaved) "Tersimpan ✅" else "Simpan API Key")
                    }
                }
            }

            // MVP Features Overview Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("🚀 Fitur Utama Aura AI Studio (MVP)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(12.dp))

                    FeatureItem(
                        icon = Icons.Default.PhotoFilter,
                        title = "Editor Foto AI Studio",
                        desc = "Filter preset (Normal, Cyber Glow, Vintage, Monokrom), penyesuaian manual kecerahan/kontras/rotasi, dan Analisis Foto otomatis berbasis Gemini Flash."
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    FeatureItem(
                        icon = Icons.Default.QuestionAnswer,
                        title = "Tanya Jawab Chatbot AI",
                        desc = "Chatbot AI serba bisa dengan dukungan lampiran foto multimodal dan riwayat percakapan tersimpan di lokal Room Database."
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    FeatureItem(
                        icon = Icons.Default.VideogameAsset,
                        title = "Pembuat Game AI Generator",
                        desc = "Generate Kuis Edukasi interaktif, Petualangan Cerita Text RPG dinamis, dan Mini Game Arcade Touch Canvas (Aura Runner)."
                    )
                }
            }

            // App Metadata Info
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("ℹ️ Informasi Sistem & Aplikasi", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Versi: 1.0.0 MVP", fontSize = 11.sp)
                    Text("Target SDK: Android 36 | Jetpack Compose M3", fontSize = 11.sp)
                    Text("Database Lokal: SQLite Room 2.6+", fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun FeatureItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    desc: String
) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Text(desc, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 15.sp)
        }
    }
}
