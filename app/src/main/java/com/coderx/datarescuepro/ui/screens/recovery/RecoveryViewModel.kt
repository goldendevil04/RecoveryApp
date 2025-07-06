package com.coderx.datarescuepro.ui.screens.recovery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RecoveryUiState(
    val isRecovering: Boolean = false,
    val isCompleted: Boolean = false,
    val progress: Float = 0f,
    val currentFile: String = "",
    val recoveredFiles: Int = 0,
    val savePath: String = "/storage/emulated/0/DataRescue/Recovered"
)

class RecoveryViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(RecoveryUiState())
    val uiState: StateFlow<RecoveryUiState> = _uiState.asStateFlow()
    
    fun startRecovery() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRecovering = true)
            
            val filesToRecover = listOf(
                "IMG_20231201_143022.jpg",
                "VID_20231130_120045.mp4",
                "Document_backup.pdf",
                "Music_track.mp3",
                "Photo_family.png"
            )
            
            filesToRecover.forEachIndexed { index, fileName ->
                delay(1000) // Simulate recovery time
                
                val progress = (index + 1).toFloat() / filesToRecover.size
                
                _uiState.value = _uiState.value.copy(
                    progress = progress,
                    currentFile = fileName,
                    recoveredFiles = index + 1
                )
            }
            
            delay(500)
            
            _uiState.value = _uiState.value.copy(
                isRecovering = false,
                isCompleted = true,
                currentFile = ""
            )
        }
    }
}