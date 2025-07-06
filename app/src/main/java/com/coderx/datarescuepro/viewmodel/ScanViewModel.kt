package com.coderx.datarescuepro.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.coderx.datarescuepro.core.FileRecoveryEngine
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ScanViewModel(application: Application) : AndroidViewModel(application) {
    private val fileRecoveryEngine = FileRecoveryEngine()
    
    private val _scanProgress = MutableStateFlow(0f)
    val scanProgress: StateFlow<Float> = _scanProgress
    
    private val _scanStatus = MutableStateFlow("Initializing...")
    val scanStatus: StateFlow<String> = _scanStatus
    
    private val _foundFiles = MutableStateFlow(0)
    val foundFiles: StateFlow<Int> = _foundFiles
    
    private val _isCompleted = MutableStateFlow(false)
    val isCompleted: StateFlow<Boolean> = _isCompleted
    
    fun startScan() {
        viewModelScope.launch {
            try {
                val isRooted = fileRecoveryEngine.detectRootAccess()
                
                fileRecoveryEngine.performFullScan(
                    context = getApplication(),
                    isRooted = isRooted,
                    onProgress = { progress, status ->
                        _scanProgress.value = progress
                        _scanStatus.value = status
                        _foundFiles.value = (progress * 250).toInt() // Simulate found files
                    }
                )
                
                delay(500) // Brief pause before completion
                _isCompleted.value = true
                
            } catch (e: Exception) {
                _scanStatus.value = "Scan failed: ${e.message}"
            }
        }
    }
}