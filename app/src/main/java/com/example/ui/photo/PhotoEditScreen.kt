package com.example.ui.photo

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoEditScreen(
    viewModel: PhotoViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) } // 0: Presets, 1: Atur Manual, 2: AI Analyst

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.loadFromUri(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Editor Foto AI Studio", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Edit, Filter & Analisis Gemini AI", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                    }
                },
                actions = {
                    IconButton(
                        onClick = { galleryLauncher.launch("image/*") },
                        modifier = Modifier.testTag("btn_gallery")
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = "Pilih Galeri")
                    }
                    IconButton(
                        onClick = { viewModel.resetFilters() },
                        modifier = Modifier.testTag("btn_reset_photo")
                    ) {
                        Icon(Icons.Default.RestartAlt, contentDescription = "Reset Foto")
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
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Sample Photos Selector Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Contoh Sampel:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Cyber", "Potret", "Sunset", "Abstrak").forEachIndexed { idx, name ->
                        FilterChip(
                            selected = false,
                            onClick = { viewModel.loadSamplePhoto(idx) },
                            label = { Text(name, fontSize = 11.sp) },
                            modifier = Modifier.testTag("sample_chip_$idx")
                        )
                    }
                }
            }

            // Image Preview Canvas Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(16.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                uiState.processedBitmap?.let { bmp ->
                    Image(
                        bitmap = bmp.asImageBitmap(),
                        contentDescription = "Hasil Edit Foto",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp)
                    )
                } ?: CircularProgressIndicator()

                if (uiState.isAiAnalyzing) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.75f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Gemini AI Sedang Menganalisis Foto...", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        }
                    }
                }
            }

            // Control Tabs (Presets, Manual, AI)
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Filter Preset") },
                    icon = { Icon(Icons.Default.AutoAwesome, contentDescription = null) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Atur Manual") },
                    icon = { Icon(Icons.Default.Tune, contentDescription = null) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("AI Analyst") },
                    icon = { Icon(Icons.Default.Psychology, contentDescription = null) }
                )
            }

            // Tab Contents
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                when (selectedTab) {
                    0 -> PresetsTabContent(viewModel, uiState)
                    1 -> ManualAdjustTabContent(viewModel, uiState)
                    2 -> AiAnalystTabContent(viewModel, uiState)
                }
            }
        }
    }
}

@Composable
fun PresetsTabContent(viewModel: PhotoViewModel, uiState: PhotoUiState) {
    LazyRow(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items(ImageUtils.defaultPresets) { preset ->
            val isSelected = uiState.selectedPreset == preset.name
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .width(130.dp)
                    .fillMaxHeight()
                    .clickable { viewModel.applyPreset(preset) }
                    .border(
                        width = if (isSelected) 2.dp else 0.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .testTag("preset_${preset.name}")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = preset.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                        )
                        if (isSelected) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Text(
                        text = preset.description,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
fun ManualAdjustTabContent(viewModel: PhotoViewModel, uiState: PhotoUiState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Brightness
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.WbSunny, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Kecerahan: ${(uiState.brightness * 100).toInt()}%", fontSize = 12.sp)
        }
        Slider(
            value = uiState.brightness,
            onValueChange = { viewModel.updateBrightness(it) },
            valueRange = 0.5f..1.5f,
            modifier = Modifier.testTag("slider_brightness")
        )

        // Contrast
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Contrast, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Kontras: ${(uiState.contrast * 100).toInt()}%", fontSize = 12.sp)
        }
        Slider(
            value = uiState.contrast,
            onValueChange = { viewModel.updateContrast(it) },
            valueRange = 0.5f..1.5f,
            modifier = Modifier.testTag("slider_contrast")
        )

        // Rotate Button
        Button(
            onClick = { viewModel.rotateImage() },
            modifier = Modifier.fillMaxWidth().testTag("btn_rotate_photo"),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        ) {
            Icon(Icons.Default.RotateRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Putar Foto 90°", color = MaterialTheme.colorScheme.onSecondaryContainer)
        }
    }
}

@Composable
fun AiAnalystTabContent(viewModel: PhotoViewModel, uiState: PhotoUiState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = uiState.aiPromptInput,
                onValueChange = { viewModel.updateAiPrompt(it) },
                placeholder = { Text("Tanyakan atau minta saran AI...", fontSize = 12.sp) },
                modifier = Modifier.weight(1f).testTag("input_ai_photo_prompt"),
                singleLine = true
            )
            Button(
                onClick = { viewModel.analyzeAndEnhanceWithAi() },
                modifier = Modifier.testTag("btn_analyze_photo"),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = "Analisis AI")
            }
        }

        // Quick Suggestions
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            AssistChip(
                onClick = { viewModel.analyzeAndEnhanceWithAi("Analisislah komposisi & pencahayaan foto ini.") },
                label = { Text("Analisis Komposisi", fontSize = 10.sp) }
            )
            AssistChip(
                onClick = { viewModel.analyzeAndEnhanceWithAi("Berikan prompt AI terinci untuk meregenerasi foto ini dalam gaya Cyberpunk.") },
                label = { Text("Prompt Cyberpunk", fontSize = 10.sp) }
            )
        }

        // AI Result Box
        uiState.aiAnalysisResult?.let { result ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("🤖 Hasil Analisis Gemini AI:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(result, fontSize = 12.sp, lineHeight = 16.sp)
                }
            }
        }
    }
}
