
package com.coderx.datarescuepro.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coderx.datarescuepro.core.FileRecoveryEngine
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ScanViewModel : ViewModel() {
    private val fileRecoveryEngine = FileRecoveryEngine()
    
    private val _scanProgress = MutableStateFlow(0f)
    val scanProgress: StateFlow<Float> = _scanProgress
    
    private val _scanStatus = MutableStateFlow("Initializing...")
    val scanStatus: StateFlow<String> = _scanStatus
    
    private val _foundFiles = MutableStateFlow(0)
    val foundFiles: StateFlow<Int> = _foundFiles
    
    fun startScan() {
        viewModelScope.launch {
            _scanStatus.value = "Preparing scan..."
            delay(1000)
            
            _scanStatus.value = "Scanning storage..."
            for (i in 1..100) {
                _scanProgress.value = i.toFloat()
                _foundFiles.value = (i * 2.5).toInt()
                
                when (i) {
                    in 1..20 -> _scanStatus.value = "Scanning system directories..."
                    in 21..40 -> _scanStatus.value = "Analyzing deleted files..."
                    in 41..60 -> _scanStatus.value = "Checking media files..."
                    in 61..80 -> _scanStatus.value = "Scanning documents..."
                    in 81..99 -> _scanStatus.value = "Finalizing results..."
                }
                
                delay(50)
            }
            
            _scanStatus.value = "completed"
        }
    }
}
