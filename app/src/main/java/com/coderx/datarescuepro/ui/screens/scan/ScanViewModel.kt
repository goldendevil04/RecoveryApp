package com.coderx.datarescuepro.ui.screens.scan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coderx.datarescuepro.core.recovery.FileRecoveryEngine
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ScanUiState(
    val isScanning: Boolean = false,
    val isCompleted: Boolean = false,
    val progress: Float = 0f,
    val currentPath: String = "",
    val foundFiles: List<String> = emptyList(),
    val fileType: String = "all"
)

class ScanViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ScanUiState())
    val uiState: StateFlow<ScanUiState> = _uiState.asStateFlow()
    
    private val recoveryEngine = FileRecoveryEngine()
    
    fun setFileType(fileType: String) {
        _uiState.value = _uiState.value.copy(fileType = fileType)
    }
    
    fun startScan() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isScanning = true)
            
            // Simulate scanning process
            val totalSteps = 100
            val foundFiles = mutableListOf<String>()
            
            for (i in 1..totalSteps) {
                delay(50) // Simulate scanning time
                
                val progress = i.toFloat() / totalSteps
                val currentPath = "/storage/emulated/0/folder_$i"
                
                // Simulate finding files
                if (i % 10 == 0) {
                    foundFiles.add("recovered_file_${foundFiles.size + 1}.${getFileExtension()}")
                }
                
                _uiState.value = _uiState.value.copy(
                    progress = progress,
                    currentPath = currentPath,
                    foundFiles = foundFiles.toList()
                )
            }
            
            _uiState.value = _uiState.value.copy(
                isScanning = false,
                isCompleted = true
            )
        }
    }
    
    private fun getFileExtension(): String {
        return when (_uiState.value.fileType) {
            "photos" -> listOf("jpg", "png", "gif").random()
            "videos" -> listOf("mp4", "avi", "mov").random()
            "documents" -> listOf("pdf", "doc", "xls").random()
            "audio" -> listOf("mp3", "wav", "aac").random()
            else -> listOf("jpg", "mp4", "pdf", "mp3").random()
        }
    }
}