package com.coderx.datarescuepro.ui.screens.results

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ResultsUiState(
    val foundFiles: List<String> = emptyList(),
    val selectedFiles: List<String> = emptyList()
)

class ResultsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ResultsUiState())
    val uiState: StateFlow<ResultsUiState> = _uiState.asStateFlow()
    
    init {
        // Simulate found files
        val mockFiles = listOf(
            "IMG_20231201_143022.jpg",
            "VID_20231130_120045.mp4",
            "Document_backup.pdf",
            "Music_track.mp3",
            "Photo_family.png",
            "Video_vacation.avi",
            "Report_final.docx",
            "Audio_recording.wav"
        )
        
        _uiState.value = _uiState.value.copy(foundFiles = mockFiles)
    }
    
    fun selectFile(fileName: String) {
        val currentSelected = _uiState.value.selectedFiles.toMutableList()
        if (!currentSelected.contains(fileName)) {
            currentSelected.add(fileName)
            _uiState.value = _uiState.value.copy(selectedFiles = currentSelected)
        }
    }
    
    fun deselectFile(fileName: String) {
        val currentSelected = _uiState.value.selectedFiles.toMutableList()
        currentSelected.remove(fileName)
        _uiState.value = _uiState.value.copy(selectedFiles = currentSelected)
    }
    
    fun selectAll() {
        _uiState.value = _uiState.value.copy(selectedFiles = _uiState.value.foundFiles)
    }
    
    fun selectNone() {
        _uiState.value = _uiState.value.copy(selectedFiles = emptyList())
    }
}