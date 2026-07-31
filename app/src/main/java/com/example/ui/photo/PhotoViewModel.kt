package com.example.ui.photo

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.PhotoFilterPreset
import com.example.data.repository.GeminiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PhotoUiState(
    val currentOriginalBitmap: Bitmap? = null,
    val processedBitmap: Bitmap? = null,
    val selectedPreset: String = "Normal",
    val brightness: Float = 1.0f,
    val contrast: Float = 1.0f,
    val saturation: Float = 1.0f,
    val rotation: Float = 0f,
    val vintage: Boolean = false,
    val cyber: Boolean = false,
    val grayscale: Boolean = false,
    val isAiAnalyzing: Boolean = false,
    val aiAnalysisResult: String? = null,
    val aiPromptInput: String = ""
)

class PhotoViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = GeminiRepository(application)

    private val _uiState = MutableStateFlow(PhotoUiState())
    val uiState: StateFlow<PhotoUiState> = _uiState.asStateFlow()

    init {
        // Load default sample photo
        loadSamplePhoto(0)
    }

    fun loadSamplePhoto(sampleType: Int) {
        val sampleBmp = SamplePhotos.createSampleBitmap(sampleType)
        _uiState.value = PhotoUiState(
            currentOriginalBitmap = sampleBmp,
            processedBitmap = sampleBmp
        )
    }

    fun loadFromUri(uri: Uri) {
        val bmp = ImageUtils.loadBitmapFromUri(getApplication(), uri)
        if (bmp != null) {
            _uiState.value = PhotoUiState(
                currentOriginalBitmap = bmp,
                processedBitmap = bmp
            )
        }
    }

    fun updateBrightness(value: Float) {
        _uiState.value = _uiState.value.copy(brightness = value)
        applyCurrentFilters()
    }

    fun updateContrast(value: Float) {
        _uiState.value = _uiState.value.copy(contrast = value)
        applyCurrentFilters()
    }

    fun updateSaturation(value: Float) {
        _uiState.value = _uiState.value.copy(saturation = value)
        applyCurrentFilters()
    }

    fun rotateImage() {
        val newRot = (_uiState.value.rotation + 90f) % 360f
        _uiState.value = _uiState.value.copy(rotation = newRot)
        applyCurrentFilters()
    }

    fun applyPreset(preset: PhotoFilterPreset) {
        _uiState.value = _uiState.value.copy(
            selectedPreset = preset.name,
            brightness = preset.brightness,
            contrast = preset.contrast,
            saturation = preset.saturation,
            vintage = preset.vintage,
            cyber = preset.cyber,
            grayscale = preset.grayscale
        )
        applyCurrentFilters()
    }

    private fun applyCurrentFilters() {
        val original = _uiState.value.currentOriginalBitmap ?: return
        val state = _uiState.value
        val result = ImageUtils.applyFilters(
            original = original,
            brightness = state.brightness,
            contrast = state.contrast,
            saturation = state.saturation,
            vintage = state.vintage,
            cyber = state.cyber,
            grayscale = state.grayscale,
            rotationDegrees = state.rotation
        )
        _uiState.value = _uiState.value.copy(processedBitmap = result)
    }

    fun updateAiPrompt(prompt: String) {
        _uiState.value = _uiState.value.copy(aiPromptInput = prompt)
    }

    fun analyzeAndEnhanceWithAi(userInstruction: String? = null) {
        val bitmapToAnalyze = _uiState.value.processedBitmap ?: _uiState.value.currentOriginalBitmap ?: return
        val prompt = userInstruction ?: _uiState.value.aiPromptInput.ifBlank {
            "Analisislah foto ini. Berikan deskripsi detail elemen visual, komposisi warna, dan 3 saran perbaikan/editing profesional."
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isAiAnalyzing = true, aiAnalysisResult = null)
            val response = repository.generateContent(
                prompt = prompt,
                bitmap = bitmapToAnalyze,
                systemInstruction = "Kamu adalah Pakar Fotografi dan AI Image Editor. Berikan analisis kritis, saran pencahayaan, serta instruksi perbaikan dalam bahasa Indonesia yang menarik dan ringkas."
            )
            _uiState.value = _uiState.value.copy(
                isAiAnalyzing = false,
                aiAnalysisResult = response
            )
        }
    }

    fun resetFilters() {
        val original = _uiState.value.currentOriginalBitmap
        _uiState.value = PhotoUiState(
            currentOriginalBitmap = original,
            processedBitmap = original
        )
    }
}
