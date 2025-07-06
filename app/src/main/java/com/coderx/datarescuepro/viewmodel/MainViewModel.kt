
package com.coderx.datarescuepro.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coderx.datarescuepro.core.FileRecoveryEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    private val fileRecoveryEngine = FileRecoveryEngine()
    
    private val _rootStatus = MutableStateFlow(false)
    val rootStatus: StateFlow<Boolean> = _rootStatus
    
    fun checkRootStatus() {
        viewModelScope.launch {
            _rootStatus.value = fileRecoveryEngine.detectRootAccess()
        }
    }
}
