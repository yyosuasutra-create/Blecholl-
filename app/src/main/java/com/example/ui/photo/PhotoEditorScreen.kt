package com.example.ui.photo

import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.MagentaNeon
import com.example.ui.theme.PurpleAccent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoEditorScreen(viewModel: PhotoViewModel) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var activeTab by remember { mutableIntStateOf(0) } // 0: Adjustments, 1: Presets, 2: AI Magic

    // Gallery Picker Launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.loadFromUri(it)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoFixHigh,
                            contentDescription = "Editor Foto AI",
                            tint = CyanPrimary,
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "AI Photo Editor Studio",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { galleryLauncher.launch("image/*") },
                        modifier = Modifier.testTag("gallery_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = "Upload Foto",
                            tint = CyanPrimary
                        )
                    }
                    IconButton(
                        onClick = { viewModel.resetFilters() },
                        modifier = Modifier.testTag("reset_filters_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset Filter"
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
            // Sample Photo Selector Row
            Text(
                text = "Pilih Sampel Foto atau Upload Gambar:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val sampleNames = listOf("City Cyber", "Portrait Studio", "Sunset Nature", "Geometry Art")
                sampleNames.forEachIndexed { idx, name ->
                    FilterChip(
                        selected = false,
                        onClick = { viewModel.loadSamplePhoto(idx) },
                        label = { Text(name, fontSize = 12.sp) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Image,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    )
                }
                AssistChip(
                    onClick = { galleryLauncher.launch("image/*") },
                    label = { Text("Upload Galeri", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                    leadingIcon = {
                        Icon(
                            Icons.Default.FileUpload,
                            contentDescription = null,
                            tint = CyanPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Photo Preview Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp)
                    .testTag("photo_preview_card"),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    val currentBmp = uiState.processedBitmap ?: uiState.currentOriginalBitmap
                    if (currentBmp != null) {
                        Image(
                            bitmap = currentBmp.asImageBitmap(),
                            contentDescription = "Pratinjau Foto Edited",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        CircularProgressIndicator(color = CyanPrimary)
                    }

                    // Floating Rotation Button
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                        ) {
                            IconButton(
                                onClick = { viewModel.rotateImage() },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.RotateRight,
                                    contentDescription = "Putar Foto",
                                    tint = CyanPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    // Bottom Preset Badge
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(12.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.90f)
                    ) {
                        Text(
                            text = "Preset: ${uiState.selectedPreset}",
                            style = MaterialTheme.typography.labelMedium,
                            color = CyanPrimary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Control Tabs
            TabRow(
                selectedTabIndex = activeTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = CyanPrimary
            ) {
                Tab(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    text = { Text("Filter & Slider", fontSize = 13.sp) },
                    icon = { Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    text = { Text("Preset AI", fontSize = 13.sp) },
                    icon = { Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = activeTab == 2,
                    onClick = { activeTab = 2 },
                    text = { Text("Analisis AI", fontSize = 13.sp) },
                    icon = { Icon(Icons.Default.Psychology, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tab Contents
            when (activeTab) {
                0 -> {
                    // Manual Sliders & Toggles
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Brightness Slider
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Kecerahan (Brightness)", style = MaterialTheme.typography.bodyMedium)
                                Text("${(uiState.brightness * 100).toInt()}%", color = CyanPrimary, fontWeight = FontWeight.Bold)
                            }
                            Slider(
                                value = uiState.brightness,
                                onValueChange = { viewModel.updateBrightness(it) },
                                valueRange = 0.5f..1.5f,
                                colors = SliderDefaults.colors(thumbColor = CyanPrimary, activeTrackColor = CyanPrimary)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Contrast Slider
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Kontras (Contrast)", style = MaterialTheme.typography.bodyMedium)
                                Text("${(uiState.contrast * 100).toInt()}%", color = PurpleAccent, fontWeight = FontWeight.Bold)
                            }
                            Slider(
                                value = uiState.contrast,
                                onValueChange = { viewModel.updateContrast(it) },
                                valueRange = 0.5f..1.5f,
                                colors = SliderDefaults.colors(thumbColor = PurpleAccent, activeTrackColor = PurpleAccent)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Saturation Slider
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Saturasi Warna", style = MaterialTheme.typography.bodyMedium)
                                Text("${(uiState.saturation * 100).toInt()}%", color = MagentaNeon, fontWeight = FontWeight.Bold)
                            }
                            Slider(
                                value = uiState.saturation,
                                onValueChange = { viewModel.updateSaturation(it) },
                                valueRange = 0.0f..2.0f,
                                colors = SliderDefaults.colors(thumbColor = MagentaNeon, activeTrackColor = MagentaNeon)
                            )
                        }
                    }
                }
                1 -> {
                    // Presets Grid
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        ImageUtils.defaultPresets.forEach { preset ->
                            val isSelected = uiState.selectedPreset == preset.name
                            OutlinedCard(
                                onClick = { viewModel.applyPreset(preset) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(
                                    if (isSelected) 2.dp else 1.dp,
                                    if (isSelected) CyanPrimary else MaterialTheme.colorScheme.outline
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = preset.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = if (isSelected) CyanPrimary else MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = preset.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = CyanPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                2 -> {
                    // AI Magic Vision Features
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Gunakan Gemini AI Multimodal untuk menganalisis dan mentransformasi foto:",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )

                        // Action Quick Chips
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ElevatedButton(
                                onClick = {
                                    viewModel.analyzeAndEnhanceWithAi("Berikan analisis detail tentang objek, komposisi, pencahayaan, dan suasana dari foto ini.")
                                },
                                modifier = Modifier.testTag("ai_analyze_button")
                            ) {
                                Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Analisis Komposisi")
                            }

                            ElevatedButton(
                                onClick = {
                                    viewModel.analyzeAndEnhanceWithAi("Berikan saran retouch profesional untuk foto ini: bagaimana tingkat kecerahan, kontras, cropping, dan warna yang ideal?")
                                }
                            ) {
                                Icon(Icons.Default.AutoFixHigh, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Saran Retouch")
                            }

                            ElevatedButton(
                                onClick = {
                                    viewModel.analyzeAndEnhanceWithAi("Buatkan 3 ide caption Instagram kreatif lengkap dengan hashtag yang cocok untuk foto ini.")
                                }
                            ) {
                                Icon(Icons.Default.Tag, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Caption IG AI")
                            }
                        }

                        // Custom Prompt Bar
                        OutlinedTextField(
                            value = uiState.aiPromptInput,
                            onValueChange = { viewModel.updateAiPrompt(it) },
                            placeholder = { Text("Tanyakan apa saja tentang foto ini...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("custom_photo_prompt_input"),
                            trailingIcon = {
                                IconButton(
                                    onClick = {
                                        if (uiState.aiPromptInput.isNotBlank()) {
                                            viewModel.analyzeAndEnhanceWithAi()
                                        }
                                    },
                                    enabled = uiState.aiPromptInput.isNotBlank() && !uiState.isAiAnalyzing
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Send,
                                        contentDescription = "Kirim Prompt",
                                        tint = if (uiState.aiPromptInput.isNotBlank()) CyanPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            shape = RoundedCornerShape(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // AI Response Display Box
            val responseText = uiState.aiAnalysisResult
            AnimatedVisibility(
                visible = uiState.isAiAnalyzing || !responseText.isNullOrBlank(),
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("ai_response_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, CyanPrimary.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Psychology,
                                    contentDescription = null,
                                    tint = CyanPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Hasil Analisis AI Gemini",
                                    fontWeight = FontWeight.Bold,
                                    color = CyanPrimary,
                                    fontSize = 15.sp
                                )
                            }
                            if (!responseText.isNullOrBlank()) {
                                IconButton(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(responseText))
                                        Toast.makeText(context, "Teks disalin ke klipbor!", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.ContentCopy,
                                        contentDescription = "Salin Teks",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        if (uiState.isAiAnalyzing) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 12.dp)
                            ) {
                                CircularProgressIndicator(
                                    color = CyanPrimary,
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "AI sedang menganalisis foto...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else if (!responseText.isNullOrBlank()) {
                            Text(
                                text = responseText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}
